package org.staymatch.pgdiscovery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.staymatch.pgdiscovery.model.FoodType;
import org.staymatch.pgdiscovery.model.PriceType;

public class CreatePgRequest {

    @NotNull(message = "Owner details are required")
    @Valid
    private OwnerProfileRequest owner;

    @NotBlank(message = "PG name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 600)
    private String description;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    @NotBlank(message = "Location label is required")
    @Size(max = 150)
    private String locationLabel;

    @Size(max = 500)
    private String fullAddress;

    @Size(max = 200)
    private String shortAddress;

    @Size(max = 100)
    private String addressCity;

    @Size(max = 100)
    private String addressState;

    @Size(max = 20)
    private String pincode;

    @NotEmpty(message = "At least one sharing option is required")
    @Valid
    private List<SharingOptionRequest> sharingOptions;

    private List<@Size(max = 80) String> facilityNames;

    @Valid
    private List<ImageRequest> images;

    private FoodType foodType;

    /** Weekly menu: day → list of meal items. e.g. {"MONDAY": ["Poha", "Dal Rice", "Roti Sabzi"]} */
    private Map<String, List<String>> weeklyMenu;

    /** Days when non-veg meals are served, e.g. ["WEDNESDAY", "SUNDAY"] */
    private List<String> nonVegDays;

    /** Days when egg-based meals are served, e.g. ["TUESDAY", "FRIDAY"] */
    private List<String> eggDays;

    private PriceType priceType;

    @DecimalMin(value = "0.0", message = "Minimum price must be >= 0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", message = "Maximum price must be >= 0")
    private BigDecimal maxPrice;

    // --- Getters / Setters ---

    public OwnerProfileRequest getOwner() {
        return owner;
    }

    public void setOwner(OwnerProfileRequest owner) {
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

    public List<SharingOptionRequest> getSharingOptions() {
        return sharingOptions;
    }

    public void setSharingOptions(List<SharingOptionRequest> sharingOptions) {
        this.sharingOptions = sharingOptions;
    }

    public List<String> getFacilityNames() {
        return facilityNames;
    }

    public void setFacilityNames(List<String> facilityNames) {
        this.facilityNames = facilityNames;
    }

    public List<ImageRequest> getImages() {
        return images;
    }

    public void setImages(List<ImageRequest> images) {
        this.images = images;
    }

    public FoodType getFoodType() {
        return foodType;
    }

    public void setFoodType(FoodType foodType) {
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

    public PriceType getPriceType() {
        return priceType;
    }

    public void setPriceType(PriceType priceType) {
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
}

