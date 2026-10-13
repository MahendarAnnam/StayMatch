package org.staymatch.pgdiscovery.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import org.staymatch.common.model.BaseEntity;

@Entity
@Table(name = "pg")
public class Pg extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private OwnerProfile owner;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 600)
    private String description;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false, length = 150)
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
    private Boolean isActive = Boolean.TRUE;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FoodType foodType;

    /** Weekly menu stored as JSON text, e.g. {"MONDAY":["Poha","Dal Rice"],...} */
    @Column(columnDefinition = "TEXT")
    private String weeklyMenu;

    /** Non-veg days stored as JSON array, e.g. ["WEDNESDAY","SUNDAY"] */
    @Column(columnDefinition = "TEXT")
    private String nonVegDays;

    /** Egg days stored as JSON array, e.g. ["TUESDAY","FRIDAY"] */
    @Column(columnDefinition = "TEXT")
    private String eggDays;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PriceType priceType = PriceType.EXACT;

    @Column(precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxPrice;

    @OneToMany(mappedBy = "pg", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SharingOption> sharingOptions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "pg", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PgFacility> pgFacilities = new LinkedHashSet<>();

    @OneToMany(mappedBy = "pg", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Image> images = new LinkedHashSet<>();

    // --- Helper methods for bidirectional sync ---

    public void addSharingOption(SharingOption option) {
        sharingOptions.add(option);
        option.setPg(this);
    }

    public void addPgFacility(PgFacility pgFacility) {
        pgFacilities.add(pgFacility);
        pgFacility.setPg(this);
    }

    public void addImage(Image image) {
        images.add(image);
        image.setPg(this);
    }

    // --- Getters / Setters ---

    public OwnerProfile getOwner() {
        return owner;
    }

    public void setOwner(OwnerProfile owner) {
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

    public FoodType getFoodType() {
        return foodType;
    }

    public void setFoodType(FoodType foodType) {
        this.foodType = foodType;
    }

    public String getWeeklyMenu() {
        return weeklyMenu;
    }

    public void setWeeklyMenu(String weeklyMenu) {
        this.weeklyMenu = weeklyMenu;
    }

    public String getNonVegDays() {
        return nonVegDays;
    }

    public void setNonVegDays(String nonVegDays) {
        this.nonVegDays = nonVegDays;
    }

    public String getEggDays() {
        return eggDays;
    }

    public void setEggDays(String eggDays) {
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

    public Set<SharingOption> getSharingOptions() {
        return sharingOptions;
    }

    public void setSharingOptions(Set<SharingOption> sharingOptions) {
        this.sharingOptions = sharingOptions;
    }

    public Set<PgFacility> getPgFacilities() {
        return pgFacilities;
    }

    public void setPgFacilities(Set<PgFacility> pgFacilities) {
        this.pgFacilities = pgFacilities;
    }

    public Set<Image> getImages() {
        return images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }
}
