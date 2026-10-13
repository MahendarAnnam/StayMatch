package org.staymatch.location.dto;

/**
 * Lightweight city response for the location picker.
 */
public class CityResponse {

    private final Long id;
    private final String name;
    private final Double lat;
    private final Double lng;

    public CityResponse(Long id, String name, Double lat, Double lng) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }
}

