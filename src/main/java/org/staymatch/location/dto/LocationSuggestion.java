package org.staymatch.location.dto;

/**
 * A location suggestion for autocomplete.
 * Can be a city, area, or specific place.
 */
public class LocationSuggestion {

    private String label;        // display text, e.g. "Kondapur, Hyderabad"
    private String description;  // secondary text from Places API
    private String placeId;      // Google place_id (null for DB-based suggestions)
    private String city;         // city name, e.g. "Hyderabad"
    private String area;         // area/locality, e.g. "Kondapur" (null for city-level)
    private Double lat;          // null when placeId is present (resolved on selection)
    private Double lng;
    private String type;         // "city", "area", or "place"

    public LocationSuggestion() {}

    public LocationSuggestion(String label, String city, String area, Double lat, Double lng, String type) {
        this.label = label;
        this.city = city;
        this.area = area;
        this.lat = lat;
        this.lng = lng;
        this.type = type;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

