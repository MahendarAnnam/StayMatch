package org.staymatch.pgdiscovery.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class SharingOptionRequest {

    @NotBlank(message = "Sharing type is required")
    @Size(max = 40)
    private String sharingType;

    @NotNull(message = "Rent is required")
    @DecimalMin(value = "0.01", message = "Rent must be greater than zero")
    private BigDecimal rent;

    @NotNull(message = "Availability count is required")
    @Min(value = 0, message = "Availability count cannot be negative")
    private Integer availabilityCount;

    public String getSharingType() {
        return sharingType;
    }

    public void setSharingType(String sharingType) {
        this.sharingType = sharingType;
    }

    public BigDecimal getRent() {
        return rent;
    }

    public void setRent(BigDecimal rent) {
        this.rent = rent;
    }

    public Integer getAvailabilityCount() {
        return availabilityCount;
    }

    public void setAvailabilityCount(Integer availabilityCount) {
        this.availabilityCount = availabilityCount;
    }
}

