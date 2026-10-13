package org.staymatch.pgdiscovery;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.staymatch.pgdiscovery.dto.CreatePgRequest;
import org.staymatch.pgdiscovery.dto.ImageRequest;
import org.staymatch.pgdiscovery.dto.OwnerProfileRequest;
import org.staymatch.pgdiscovery.dto.PgResponse;
import org.staymatch.pgdiscovery.dto.PgSearchCriteria;
import org.staymatch.pgdiscovery.dto.SharingOptionRequest;
import org.staymatch.pgdiscovery.service.PgSearchService;
import org.staymatch.pgdiscovery.service.PgService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PgDiscoveryIntegrationTest {

    @Autowired
    private PgService pgService;

    @Autowired
    private PgSearchService pgSearchService;

    @Test
    void shouldCreateAndSearchPg() {
        // --- 1. Create a PG ---
        CreatePgRequest request = buildRequest();
        PgResponse created = pgService.createPg(request);

        assertNotNull(created.getId());
        assertEquals("Sunshine PG", created.getName());
        assertEquals(2, created.getSharingOptions().size());
        assertEquals(List.of("WiFi", "AC"), created.getFacilities());
        assertEquals(1, created.getImages().size());

        // --- 2. Get by id ---
        PgResponse fetched = pgService.getPgById(created.getId());
        assertEquals(created.getId(), fetched.getId());

        // --- 3. Search by sharing type ---
        PgSearchCriteria criteria = new PgSearchCriteria();
        criteria.setSharingType("Double");
        Page<PgResponse> results = pgSearchService.searchPgs(criteria, PageRequest.of(0, 10));
        assertFalse(results.isEmpty());

        // --- 4. Search by price range ---
        PgSearchCriteria priceCriteria = new PgSearchCriteria();
        priceCriteria.setMinPrice(new BigDecimal("4000"));
        priceCriteria.setMaxPrice(new BigDecimal("9000"));
        Page<PgResponse> priceResults = pgSearchService.searchPgs(priceCriteria, PageRequest.of(0, 10));
        assertFalse(priceResults.isEmpty());

        // --- 5. Search by location ---
        PgSearchCriteria geoCriteria = new PgSearchCriteria();
        geoCriteria.setLatitude(12.9700);
        geoCriteria.setLongitude(77.5900);
        geoCriteria.setRadiusKm(5.0);
        Page<PgResponse> geoResults = pgSearchService.searchPgs(geoCriteria, PageRequest.of(0, 10));
        assertFalse(geoResults.isEmpty());
        assertNotNull(geoResults.getContent().get(0).getDistanceKm());

        // --- 6. Search by facility ---
        PgSearchCriteria facilityCriteria = new PgSearchCriteria();
        facilityCriteria.setFacilityNames(List.of("WiFi"));
        Page<PgResponse> facilityResults = pgSearchService.searchPgs(facilityCriteria, PageRequest.of(0, 10));
        assertFalse(facilityResults.isEmpty());
    }

    private CreatePgRequest buildRequest() {
        OwnerProfileRequest owner = new OwnerProfileRequest();
        owner.setFullName("Ramesh Kumar");
        owner.setEmail("ramesh@example.com");
        owner.setPhoneNumber("9876543210");

        SharingOptionRequest single = new SharingOptionRequest();
        single.setSharingType("Single");
        single.setRent(new BigDecimal("10000"));
        single.setAvailabilityCount(3);

        SharingOptionRequest dbl = new SharingOptionRequest();
        dbl.setSharingType("Double");
        dbl.setRent(new BigDecimal("6000"));
        dbl.setAvailabilityCount(5);

        ImageRequest image = new ImageRequest();
        image.setImageUrl("https://example.com/pg1.jpg");
        image.setDisplayOrder(1);

        CreatePgRequest request = new CreatePgRequest();
        request.setOwner(owner);
        request.setName("Sunshine PG");
        request.setDescription("Comfortable PG near tech park");
        request.setLatitude(12.9716);
        request.setLongitude(77.5946);
        request.setLocationLabel("Koramangala, Bangalore");
        request.setSharingOptions(List.of(single, dbl));
        request.setFacilityNames(List.of("WiFi", "AC"));
        request.setImages(List.of(image));

        return request;
    }
}

