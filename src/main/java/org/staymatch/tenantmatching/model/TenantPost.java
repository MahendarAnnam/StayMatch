package org.staymatch.tenantmatching.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.staymatch.common.model.BaseEntity;

@Entity
@Table(name = "tenant_posts")
public class TenantPost extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minBudget;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxBudget;

    @Column(nullable = false)
    private Double preferredLatitude;

    @Column(nullable = false)
    private Double preferredLongitude;

    @Column(nullable = false, length = 120)
    private String locationLabel;

    @Column(length = 500)
    private String fullAddress;

    @Column(length = 200)
    private String shortAddress;

    @Column(length = 100)
    private String addressCity;

    @Column(length = 100)
    private String addressState;

    @Column(length = 20)
    private String pincode;

    @Column(nullable = false)
    private LocalDate moveInDate;

    @Column(nullable = false)
    private Integer totalCapacity = 1;

    @Column(nullable = false)
    private Integer currentOccupancy = 0;

    @Column(nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "preference_id", unique = true)
    private Preference preference;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Preference getPreference() {
        return preference;
    }

    public void setPreference(Preference preference) {
        this.preference = preference;
    }
}

