package org.staymatch.pgdiscovery.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Captures all search/filter parameters for PG search.
 */
public class PgSearchCriteria {

    private Double latitude;
    private Double longitude;
    private Double radiusKm;
    private String city;
    private String area;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sharingType;
    private List<String> facilityNames;
    private String foodType;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(Double radiusKm) {
        this.radiusKm = radiusKm;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getSharingType() {
        return sharingType;
    }

    public void setSharingType(String sharingType) {
        this.sharingType = sharingType;
    }

    public List<String> getFacilityNames() {
        return facilityNames;
    }

    public void setFacilityNames(List<String> facilityNames) {
        this.facilityNames = facilityNames;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }
}

