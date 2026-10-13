package org.staymatch.location.dto;

/**
 * Resolved place details from Google Place Details API.
 */
public class PlaceDetailsResponse {

    private String placeId;
    private String label;
    private String fullAddress;
    private String shortAddress;
    private String city;
    private String area;
    private String state;
    private String pincode;
    private Double lat;
    private Double lng;
    private String type;  // "city" or "area"

    public PlaceDetailsResponse() {}

    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public String getShortAddress() { return shortAddress; }
    public void setShortAddress(String shortAddress) { this.shortAddress = shortAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

