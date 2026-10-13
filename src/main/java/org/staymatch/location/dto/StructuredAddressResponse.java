package org.staymatch.location.dto;

/**
 * Structured address resolved via reverse geocoding.
 */
public class StructuredAddressResponse {

    private String fullAddress;
    private String shortAddress;
    private String city;
    private String state;
    private String pincode;
    private Double lat;
    private Double lng;

    public StructuredAddressResponse() {}

    public StructuredAddressResponse(String fullAddress, String shortAddress,
                                     String city, String state, String pincode,
                                     Double lat, Double lng) {
        this.fullAddress = fullAddress;
        this.shortAddress = shortAddress;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.lat = lat;
        this.lng = lng;
    }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public String getShortAddress() { return shortAddress; }
    public void setShortAddress(String shortAddress) { this.shortAddress = shortAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
}

