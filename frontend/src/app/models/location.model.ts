/** Model for a city returned by GET /api/v1/locations/cities */
export interface CityLocation {
  id: number;
  name: string;
  lat: number;
  lng: number;
}

/** Structured result from reverse-geocoding a lat/lng pair. */
export interface GeocodedAddress {
  /** Formatted display string, e.g. "Kondapur, Hyderabad (500083)" */
  formattedAddress: string;
  /** Full Google-formatted address */
  fullAddress: string;
  /** Short display, e.g. "Kondapur, Hyderabad" */
  shortAddress: string;
  /** Suburb / neighbourhood, e.g. "Kondapur" */
  area: string;
  /** Road / street / locality */
  street: string;
  /** Postal code, e.g. "500083" */
  pincode: string;
  /** City / town */
  city: string;
  /** State, e.g. "Telangana" */
  state: string;
}

/** A nearby area suggestion returned by getNearbyAreas(). */
export interface NearbyArea {
  name: string;
  latitude: number;
  longitude: number;
  /** Approximate distance in kilometres from the queried point. */
  distanceKm: number;
}

/** Coordinates resolved by the location picker. */
export interface ResolvedLocation {
  latitude: number;
  longitude: number;
  label: string;            // city name or "Current Location"
  address?: string;         // formatted display address, e.g. "Kondapur, Hyderabad (500083)"
  fullAddress?: string;     // full Google-formatted address
  shortAddress?: string;    // e.g. "Kondapur, Hyderabad"
  area?: string;            // suburb / neighbourhood, e.g. "Kondapur"
  street?: string;          // road / street / locality
  pincode?: string;         // postal code
  city?: string;            // city
  state?: string;           // state
  type?: 'city' | 'area' | 'gps';  // how the location was resolved
}

/** A location autocomplete suggestion from backend. */
export interface LocationSuggestion {
  label: string;
  description?: string;
  placeId?: string;       // Google place_id (null for DB-based suggestions)
  city?: string;
  area?: string;
  lat?: number;           // null when placeId is present (resolved on selection)
  lng?: number;
  type: 'city' | 'area' | 'place';
}

/** Resolved place details from backend Place Details API. */
export interface PlaceDetailsResult {
  placeId: string;
  label: string;
  fullAddress: string;
  city: string;
  area?: string;
  state: string;
  pincode: string;
  lat: number;
  lng: number;
  type: 'city' | 'area';
}

