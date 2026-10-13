package org.staymatch.location.service;

import java.util.List;
import org.staymatch.location.dto.LocationSuggestion;
import org.staymatch.location.dto.PlaceDetailsResponse;

/**
 * Provides location search/autocomplete suggestions.
 */
public interface LocationSearchService {

    /**
     * Search for locations matching the given query string.
     * Returns both city-level and area-level suggestions.
     *
     * @param query  partial text (e.g. "Kon", "Hyd", "Bangalore")
     * @param limit  max results
     * @return list of matching suggestions
     */
    List<LocationSuggestion> search(String query, int limit);

    /**
     * Resolve a Google place_id into full location details (lat/lng/city/area).
     *
     * @param placeId Google Place ID
     * @return resolved place details
     */
    PlaceDetailsResponse getPlaceDetails(String placeId);
}
