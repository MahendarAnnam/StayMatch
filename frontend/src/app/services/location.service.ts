import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay, switchMap } from 'rxjs/operators';
import {
  CityLocation,
  GeocodedAddress,
  LocationSuggestion,
  NearbyArea,
  PlaceDetailsResult,
  ResolvedLocation
} from '../models/location.model';
import { environment } from '../../environments/environment';

// ── Nominatim response shapes (internal) ──────────────────────────────

/** Subset of the Nominatim reverse-geocode JSON response. */
interface NominatimReverseResponse {
  address: {
    road?: string;
    neighbourhood?: string;
    suburb?: string;
    city?: string;
    town?: string;
    village?: string;
    county?: string;
    state_district?: string;
    state?: string;
    postcode?: string;
    country?: string;
    [key: string]: string | undefined;
  };
  display_name: string;
}

/** Single result item from Nominatim search (used for nearby areas). */
interface NominatimSearchResult {
  display_name: string;
  lat: string;
  lon: string;
  address?: {
    suburb?: string;
    neighbourhood?: string;
    village?: string;
    city?: string;
    town?: string;
    [key: string]: string | undefined;
  };
}

/** Response shape from backend GET /api/v1/locations/reverse-geocode */
interface BackendGeoResponse {
  fullAddress: string;
  shortAddress: string;
  city: string;
  state: string;
  pincode: string;
  lat: number;
  lng: number;
}

// ── Service ───────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class LocationService {

  private readonly apiBaseUrl = `${environment.apiBaseUrl}/locations`;
  private readonly nominatimUrl = 'https://nominatim.openstreetmap.org';

  /** Cached cities observable — fetched once, shared across subscribers. */
  private cities$: Observable<CityLocation[]> | null = null;

  constructor(private readonly http: HttpClient) {}

  // ── searchLocations (autocomplete) ─────────────────────────────────

  /**
   * Searches locations by query string for autocomplete.
   * Returns city and area-level suggestions from backend.
   */
  searchLocations(query: string, limit: number = 10): Observable<LocationSuggestion[]> {
    const params = { query, limit: String(limit) };
    return this.http.get<LocationSuggestion[]>(`${this.apiBaseUrl}/search`, { params });
  }

  /**
   * Resolves a Google place_id into full location details (lat/lng/city/area).
   * Called when the user selects a Places Autocomplete suggestion.
   */
  getPlaceDetails(placeId: string): Observable<PlaceDetailsResult> {
    return this.http.get<PlaceDetailsResult>(`${this.apiBaseUrl}/place-details`, {
      params: { placeId }
    });
  }

  // ── getCities (backend API — cached) ────────────────────────────────

  /** Fetches all supported cities from backend (cached after first call). */
  getCities(): Observable<CityLocation[]> {
    if (!this.cities$) {
      this.cities$ = this.http
        .get<CityLocation[]>(`${this.apiBaseUrl}/cities`)
        .pipe(shareReplay(1));
    }
    return this.cities$;
  }

  // ── getCurrentLocation ──────────────────────────────────────────────

  /**
   * Gets the user's current GPS position via the browser Geolocation API,
   * then automatically enriches it with a human-readable address via
   * reverse geocoding.
   *
   * Emits a single fully-enriched `ResolvedLocation` and completes.
   */
  getCurrentLocation(): Observable<ResolvedLocation> {
    return this.getBrowserPosition().pipe(
      switchMap((coords) =>
        this.getAddressFromLatLng(coords.latitude, coords.longitude).pipe(
          map((geo) => this.enrichLocation(coords, geo))
        )
      )
    );
  }

  // ── getAddressFromLatLng ────────────────────────────────────────────

  /**
   * Reverse-geocodes a lat/lng pair into a structured address.
   * Primary: backend API (which may use Google Maps).
   * Fallback: OpenStreetMap Nominatim (client-side).
   */
  getAddressFromLatLng(lat: number, lng: number): Observable<GeocodedAddress> {
    return this.reverseGeocodeViaBackend(lat, lng).pipe(
      catchError(() => this.reverseGeocodeViaNominatim(lat, lng))
    );
  }

  /**
   * Calls the backend reverse-geocode endpoint.
   */
  private reverseGeocodeViaBackend(lat: number, lng: number): Observable<GeocodedAddress> {
    const url = `${this.apiBaseUrl}/reverse-geocode?lat=${lat}&lng=${lng}`;
    return this.http.get<BackendGeoResponse>(url).pipe(
      map((res) => {
        const shortAddress = res.shortAddress || '';
        const formattedAddress = res.pincode
          ? `${shortAddress} (${res.pincode})`
          : shortAddress;
        return {
          formattedAddress: formattedAddress || res.fullAddress,
          fullAddress: res.fullAddress || formattedAddress,
          shortAddress,
          area: shortAddress.split(',')[0]?.trim() || '',
          street: '',
          pincode: res.pincode || '',
          city: res.city || '',
          state: res.state || ''
        } as GeocodedAddress;
      })
    );
  }

  /**
   * Fallback: Nominatim reverse geocoding.
   */
  private reverseGeocodeViaNominatim(lat: number, lng: number): Observable<GeocodedAddress> {
    const url =
      `${this.nominatimUrl}/reverse?lat=${lat}&lon=${lng}&format=json&addressdetails=1`;

    return this.http
      .get<NominatimReverseResponse>(url, {
        headers: { 'Accept-Language': 'en' }
      })
      .pipe(
        map((res) => this.parseNominatimAddress(res, lat, lng))
      );
  }

  // ── getNearbyAreas ──────────────────────────────────────────────────

  /**
   * Returns a list of nearby suburbs / areas around the given coordinates
   * using the Nominatim search API with a bounded viewbox.
   *
   * Useful for showing area suggestions or a "Nearby areas" list.
   *
   * @param lat  Centre latitude
   * @param lng  Centre longitude
   * @param radiusKm  Search radius in kilometres (default 5)
   * @param limit  Max number of results (default 6)
   */
  getNearbyAreas(
    lat: number,
    lng: number,
    radiusKm: number = 5,
    limit: number = 6
  ): Observable<NearbyArea[]> {
    // Convert radius to a rough degree offset (~0.009° per km at Indian latitudes)
    const degOffset = radiusKm * 0.009;

    const viewbox = [
      lng - degOffset,   // west
      lat + degOffset,   // north
      lng + degOffset,   // east
      lat - degOffset    // south
    ].join(',');

    const url =
      `${this.nominatimUrl}/search?format=json&addressdetails=1` +
      `&viewbox=${viewbox}&bounded=1&limit=${limit}` +
      `&featuretype=settlement`;

    return this.http
      .get<NominatimSearchResult[]>(url, {
        headers: { 'Accept-Language': 'en' }
      })
      .pipe(
        map((results) =>
          results.map((r) => {
            const rLat = parseFloat(r.lat);
            const rLng = parseFloat(r.lon);
            return {
              name:
                r.address?.suburb ||
                r.address?.neighbourhood ||
                r.address?.village ||
                r.display_name.split(',')[0],
              latitude: rLat,
              longitude: rLng,
              distanceKm: this.haversineKm(lat, lng, rLat, rLng)
            } as NearbyArea;
          })
        ),
        // Sort by distance ascending
        map((areas) => areas.sort((a, b) => a.distanceKm - b.distanceKm))
      );
  }

  // ── resolveLocationWithAddress (orchestration helper) ───────────────

  /**
   * Takes a raw `ResolvedLocation` (with just lat/lng/label) and enriches
   * it with reverse-geocoded address fields.
   *
   * Convenience method so components don't have to manually chain
   * getAddressFromLatLng + field mapping.
   */
  resolveLocationWithAddress(loc: ResolvedLocation): Observable<ResolvedLocation> {
    return this.getAddressFromLatLng(loc.latitude, loc.longitude).pipe(
      map((geo) => this.enrichLocation(loc, geo))
    );
  }

  // ── Private helpers ─────────────────────────────────────────────────

  /**
   * Wraps the browser Geolocation API in an Observable.
   * Emits a basic ResolvedLocation with lat/lng and label "Current Location".
   */
  private getBrowserPosition(): Observable<ResolvedLocation> {
    return new Observable<ResolvedLocation>((observer) => {
      if (!navigator.geolocation) {
        observer.error('Geolocation is not supported by this browser.');
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          observer.next({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            label: 'Current Location'
          });
          observer.complete();
        },
        (error) => {
          let msg: string;
          switch (error.code) {
            case error.PERMISSION_DENIED:
              msg = 'Location permission denied. Please select a city instead.';
              break;
            case error.POSITION_UNAVAILABLE:
              msg = 'Location information unavailable.';
              break;
            case error.TIMEOUT:
              msg = 'Location request timed out.';
              break;
            default:
              msg = 'An unknown error occurred while fetching location.';
          }
          observer.error(msg);
        },
        { enableHighAccuracy: false, timeout: 10000, maximumAge: 300000 }
      );
    });
  }

  /** Parses a Nominatim reverse-geocode response into a GeocodedAddress. */
  private parseNominatimAddress(
    res: NominatimReverseResponse,
    lat: number,
    lng: number
  ): GeocodedAddress {
    const addr = res.address;

    const area    = addr.suburb || addr.neighbourhood || addr.village || '';
    const street  = addr.road || addr.neighbourhood || '';
    const city    = addr.city || addr.town || addr.county || addr.state_district || '';
    const state   = addr.state || '';
    const pincode = addr.postcode || '';

    // Build display address like "Kondapur, Hyderabad (500083)"
    const parts: string[] = [];
    if (area)                    parts.push(area);
    if (city && city !== area)   parts.push(city);
    let formattedAddress = parts.join(', ');
    if (pincode) formattedAddress += ` (${pincode})`;

    const shortAddress = parts.join(', ');
    const fullAddress = res.display_name || formattedAddress;

    // Fallback if everything is empty
    if (!formattedAddress.trim()) {
      formattedAddress = res.display_name || `${lat.toFixed(4)}, ${lng.toFixed(4)}`;
    }

    return { formattedAddress, fullAddress, shortAddress, area, street, pincode, city, state };
  }

  /** Merges a raw ResolvedLocation with geocoded address fields. */
  private enrichLocation(loc: ResolvedLocation, geo: GeocodedAddress): ResolvedLocation {
    return {
      ...loc,
      label: geo.formattedAddress || loc.label,
      address: geo.formattedAddress,
      fullAddress: geo.fullAddress,
      shortAddress: geo.shortAddress,
      area: geo.area,
      street: geo.street,
      pincode: geo.pincode,
      city: geo.city,
      state: geo.state
    };
  }

  /** Haversine formula — distance (km) between two lat/lng pairs. */
  private haversineKm(
    lat1: number, lon1: number,
    lat2: number, lon2: number
  ): number {
    const R = 6371; // Earth radius in km
    const dLat = this.toRad(lat2 - lat1);
    const dLon = this.toRad(lon2 - lon1);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.toRad(lat1)) * Math.cos(this.toRad(lat2)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  private toRad(deg: number): number {
    return deg * (Math.PI / 180);
  }
}

