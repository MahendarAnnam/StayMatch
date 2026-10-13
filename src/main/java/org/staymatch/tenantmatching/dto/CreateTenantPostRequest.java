package org.staymatch.tenantmatching.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateTenantPostRequest {

    @NotNull(message = "User details are required")
    @Valid
    private CreateUserRequest user;

    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title must be at most 120 characters")
    private String title;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @NotNull(message = "Minimum budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Minimum budget must be greater than zero")
    private BigDecimal minBudget;

    @NotNull(message = "Maximum budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Maximum budget must be greater than zero")
    private BigDecimal maxBudget;

    @NotNull(message = "Preferred latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double preferredLatitude;

    @NotNull(message = "Preferred longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double preferredLongitude;

    @NotBlank(message = "Location label is required")
    @Size(max = 120, message = "Location label must be at most 120 characters")
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

    @NotNull(message = "Move-in date is required")
    private LocalDate moveInDate;

    @NotNull(message = "Total capacity is required")
    @Min(value = 1, message = "Total capacity must be at least 1")
    private Integer totalCapacity;

    @NotNull(message = "Current occupancy is required")
    @Min(value = 0, message = "Current occupancy cannot be negative")
    private Integer currentOccupancy;

    @NotNull(message = "Preference details are required")
    @Valid
    private PreferenceRequest preference;

    public CreateUserRequest getUser() {
        return user;
    }

    public void setUser(CreateUserRequest user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getMinBudget() {
        return minBudget;
    }

    public void setMinBudget(BigDecimal minBudget) {
        this.minBudget = minBudget;
    }

    public BigDecimal getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(BigDecimal maxBudget) {
        this.maxBudget = maxBudget;
    }

    public Double getPreferredLatitude() {
        return preferredLatitude;
    }

    public void setPreferredLatitude(Double preferredLatitude) {
        this.preferredLatitude = preferredLatitude;
    }

    public Double getPreferredLongitude() {
        return preferredLongitude;
    }

    public void setPreferredLongitude(Double preferredLongitude) {
        this.preferredLongitude = preferredLongitude;
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

    public LocalDate getMoveInDate() {
        return moveInDate;
    }

    public void setMoveInDate(LocalDate moveInDate) {
        this.moveInDate = moveInDate;
    }

    public Integer getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public Integer getCurrentOccupancy() {
        return currentOccupancy;
    }

    public void setCurrentOccupancy(Integer currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    public PreferenceRequest getPreference() {
        return preference;
    }

    public void setPreference(PreferenceRequest preference) {
        this.preference = preference;
    }
}

