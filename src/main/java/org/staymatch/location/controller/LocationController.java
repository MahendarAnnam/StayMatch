package org.staymatch.location.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.staymatch.location.dto.CityResponse;
import org.staymatch.location.dto.LocationSuggestion;
import org.staymatch.location.dto.PlaceDetailsResponse;
import org.staymatch.location.dto.StructuredAddressResponse;
import org.staymatch.location.service.CityService;
import org.staymatch.location.service.GeocodingService;
import org.staymatch.location.service.LocationSearchService;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final CityService cityService;
    private final GeocodingService geocodingService;
    private final LocationSearchService locationSearchService;

    public LocationController(CityService cityService,
                              GeocodingService geocodingService,
                              LocationSearchService locationSearchService) {
        this.cityService = cityService;
        this.geocodingService = geocodingService;
        this.locationSearchService = locationSearchService;
    }

    /**
     * Returns all supported cities with their coordinates.
     * Used by the frontend location picker.
     */
    @GetMapping("/cities")
    public ResponseEntity<List<CityResponse>> getCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    /**
     * Reverse-geocodes lat/lng into a structured address.
     */
    @GetMapping("/reverse-geocode")
    public ResponseEntity<StructuredAddressResponse> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lng) {
        return ResponseEntity.ok(geocodingService.reverseGeocode(lat, lng));
    }

    /**
     * Search locations by query string for autocomplete.
     * Returns city and area-level suggestions.
     */
    @GetMapping("/search")
    public ResponseEntity<List<LocationSuggestion>> searchLocations(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        return ResponseEntity.ok(locationSearchService.search(query, limit));
    }

    /**
     * Resolve a Google place_id into full location details.
     * Called when user selects a suggestion from autocomplete.
     */
    @GetMapping("/place-details")
    public ResponseEntity<PlaceDetailsResponse> getPlaceDetails(
            @RequestParam String placeId) {
        return ResponseEntity.ok(locationSearchService.getPlaceDetails(placeId));
    }
}
