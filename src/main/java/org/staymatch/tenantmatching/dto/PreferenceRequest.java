package org.staymatch.tenantmatching.dto;

import jakarta.validation.constraints.Size;

public class PreferenceRequest {

    @Size(max = 50, message = "Food preference must be at most 50 characters")
    private String foodPreference;

    @Size(max = 50, message = "Smoking preference must be at most 50 characters")
    private String smokingPreference;

    @Size(max = 50, message = "Preferred gender must be at most 50 characters")
    private String preferredGender;

    @Size(max = 300, message = "Additional notes must be at most 300 characters")
    private String additionalNotes;

    public String getFoodPreference() {
        return foodPreference;
    }

    public void setFoodPreference(String foodPreference) {
        this.foodPreference = foodPreference;
    }

    public String getSmokingPreference() {
        return smokingPreference;
    }

    public void setSmokingPreference(String smokingPreference) {
        this.smokingPreference = smokingPreference;
    }

    public String getPreferredGender() {
        return preferredGender;
    }

    public void setPreferredGender(String preferredGender) {
        this.preferredGender = preferredGender;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
}

