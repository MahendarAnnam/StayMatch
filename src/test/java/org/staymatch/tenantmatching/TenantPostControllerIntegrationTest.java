package org.staymatch.tenantmatching;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.staymatch.tenantmatching.dto.CreateTenantPostRequest;
import org.staymatch.tenantmatching.dto.CreateUserRequest;
import org.staymatch.tenantmatching.dto.PreferenceRequest;
import org.staymatch.tenantmatching.dto.TenantPostResponse;
import org.staymatch.tenantmatching.dto.TenantPostSearchCriteria;
import org.staymatch.tenantmatching.service.TenantPostService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TenantPostControllerIntegrationTest {

    @Autowired
    private TenantPostService tenantPostService;

    @Test
    void shouldCreateFetchAndSearchTenantPost() {
        CreateTenantPostRequest request = new CreateTenantPostRequest();

        CreateUserRequest user = new CreateUserRequest();
        user.setFullName("Aman Verma");
        user.setEmail("aman@example.com");
        user.setPhoneNumber("9999999999");

        PreferenceRequest preference = new PreferenceRequest();
        preference.setFoodPreference("Vegetarian");
        preference.setSmokingPreference("Non-Smoker");
        preference.setPreferredGender("Male");
        preference.setAdditionalNotes("Looking for clean roommates");

        request.setUser(user);
        request.setTitle("Need roommate near Whitefield");
        request.setDescription("Working professional, quiet schedule");
        request.setMinBudget(new BigDecimal("7000"));
        request.setMaxBudget(new BigDecimal("12000"));
        request.setTotalCapacity(3);
        request.setCurrentOccupancy(1);
        request.setPreferredLatitude(12.9698);
        request.setPreferredLongitude(77.7500);
        request.setLocationLabel("Whitefield, Bangalore");
        request.setMoveInDate(LocalDate.now().plusDays(5));
        request.setPreference(preference);

        TenantPostResponse created = tenantPostService.createTenantPost(request);
        assertNotNull(created.getId());

        TenantPostResponse fetched = tenantPostService.getTenantPostById(created.getId());
        assertEquals("Need roommate near Whitefield", fetched.getTitle());
        assertEquals("aman@example.com", fetched.getUser().getEmail());

        TenantPostSearchCriteria criteria = new TenantPostSearchCriteria();
        criteria.setBudget(new BigDecimal("9000"));
        criteria.setLatitude(12.9700);
        criteria.setLongitude(77.7502);
        criteria.setRadiusKm(5.0);

        List<TenantPostResponse> searchResults = tenantPostService.searchTenantPosts(criteria);
        assertFalse(searchResults.isEmpty());
    }
}

