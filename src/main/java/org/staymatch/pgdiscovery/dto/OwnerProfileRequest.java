package org.staymatch.pgdiscovery.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OwnerProfileRequest {

    @NotBlank(message = "Owner name is required")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Owner email is required")
    @Email(message = "Email must be valid")
    @Size(max = 120)
    private String email;

    @Size(max = 20)
    private String phoneNumber;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

