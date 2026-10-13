package org.staymatch.tenantmatching.mapper;

import org.springframework.stereotype.Component;
import org.staymatch.tenantmatching.dto.PreferenceRequest;
import org.staymatch.tenantmatching.dto.PreferenceResponse;
import org.staymatch.tenantmatching.model.Preference;

@Component
public class PreferenceMapper {

    public Preference toEntity(PreferenceRequest request) {
        Preference preference = new Preference();
        preference.setFoodPreference(request.getFoodPreference());
        preference.setSmokingPreference(request.getSmokingPreference());
        preference.setPreferredGender(request.getPreferredGender());
        preference.setAdditionalNotes(request.getAdditionalNotes());
        return preference;
    }

    public PreferenceResponse toResponse(Preference preference) {
        PreferenceResponse response = new PreferenceResponse();
        response.setId(preference.getId());
        response.setFoodPreference(preference.getFoodPreference());
        response.setSmokingPreference(preference.getSmokingPreference());
        response.setPreferredGender(preference.getPreferredGender());
        response.setAdditionalNotes(preference.getAdditionalNotes());
        return response;
    }
}

