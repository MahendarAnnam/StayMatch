package org.staymatch.location.service;

import org.staymatch.location.dto.StructuredAddressResponse;

/**
 * Reverse-geocodes lat/lng into structured address components.
 */
public interface GeocodingService {

    /**
     * Reverse-geocode the given coordinates into a structured address.
     *
     * @param lat latitude
     * @param lng longitude
     * @return structured address, never null (falls back to coordinate-based label)
     */
    StructuredAddressResponse reverseGeocode(double lat, double lng);
}

