package org.staymatch.pgdiscovery.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class PgResponse {

    private Long id;
    private OwnerProfileResponse owner;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String locationLabel;
    private String fullAddress;
    private String shortAddress;
    private String addressCity;
    private String addressState;
    private String pincode;
    private Boolean isActive;
    private List<SharingOptionResponse> sharingOptions;
    private List<String> facilities;
    private List<ImageResponse> images;
    private Double distanceKm;
    private String foodType;
    private Map<String, List<String>> weeklyMenu;
    private List<String> nonVegDays;
    private List<String> eggDays;
    private String priceType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Instant createdAt;
    private Instant updatedAt;

    // --- Getters / Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OwnerProfileResponse getOwner() {
        return owner;
    }

    public void setOwner(OwnerProfileResponse owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    public String getLocationLabel() {
        return locationLabel;
    }

    public void setLocationLabel(String locationLabel) {
        this.locationLabel = locationLabel;
    }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public String getShortAddress() { return shortAddress; }
    public void setShortAddress(String shortAddress) { this.shortAddress = shortAddress; }

    public String getAddressCity() { return addressCity; }
    public void setAddressCity(String addressCity) { this.addressCity = addressCity; }

    public String getAddressState() { return addressState; }
    public void setAddressState(String addressState) { this.addressState = addressState; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public List<SharingOptionResponse> getSharingOptions() {
        return sharingOptions;
    }

    public void setSharingOptions(List<SharingOptionResponse> sharingOptions) {
        this.sharingOptions = sharingOptions;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities;
    }

    public List<ImageResponse> getImages() {
        return images;
    }

    public void setImages(List<ImageResponse> images) {
        this.images = images;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public Map<String, List<String>> getWeeklyMenu() {
        return weeklyMenu;
    }

    public void setWeeklyMenu(Map<String, List<String>> weeklyMenu) {
        this.weeklyMenu = weeklyMenu;
    }

    public List<String> getNonVegDays() {
        return nonVegDays;
    }

    public void setNonVegDays(List<String> nonVegDays) {
        this.nonVegDays = nonVegDays;
    }

    public List<String> getEggDays() {
        return eggDays;
    }

    public void setEggDays(List<String> eggDays) {
        this.eggDays = eggDays;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

