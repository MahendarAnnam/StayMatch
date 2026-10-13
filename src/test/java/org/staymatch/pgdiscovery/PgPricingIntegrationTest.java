package org.staymatch.pgdiscovery;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.staymatch.pgdiscovery.dto.CreatePgRequest;
import org.staymatch.pgdiscovery.dto.OwnerProfileRequest;
import org.staymatch.pgdiscovery.dto.PgResponse;
import org.staymatch.pgdiscovery.dto.SharingOptionRequest;
import org.staymatch.pgdiscovery.model.PriceType;
import org.staymatch.pgdiscovery.service.PgService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the flexible pricing feature (PriceType, minPrice, maxPrice).
 */
@SpringBootTest
class PgPricingIntegrationTest {

    @Autowired
    private PgService pgService;

    @Test
    void shouldCreatePgWithExactPricing() {
        CreatePgRequest request = buildBaseRequest("exact-pricing-" + System.nanoTime() + "@test.com");
        request.setPriceType(PriceType.EXACT);

        PgResponse created = pgService.createPg(request);

        assertNotNull(created.getId());
        assertEquals("EXACT", created.getPriceType());
        assertNull(created.getMinPrice());
        assertNull(created.getMaxPrice());
    }

    @Test
    void shouldCreatePgWithRangePricing() {
        CreatePgRequest request = buildBaseRequest("range-pricing-" + System.nanoTime() + "@test.com");
        request.setPriceType(PriceType.RANGE);
        request.setMinPrice(new BigDecimal("4000"));
        request.setMaxPrice(new BigDecimal("7000"));

        PgResponse created = pgService.createPg(request);

        assertNotNull(created.getId());
        assertEquals("RANGE", created.getPriceType());
        // Use compareTo to avoid scale mismatch (4000 vs 4000.00)
        assertTrue(new BigDecimal("4000").compareTo(created.getMinPrice()) == 0,
            "minPrice should be 4000");
        assertTrue(new BigDecimal("7000").compareTo(created.getMaxPrice()) == 0,
            "maxPrice should be 7000");
    }

    @Test
    void shouldCreatePgWithHiddenPricing() {
        CreatePgRequest request = buildBaseRequest("hidden-pricing-" + System.nanoTime() + "@test.com");
        request.setPriceType(PriceType.HIDDEN);

        PgResponse created = pgService.createPg(request);

        assertNotNull(created.getId());
        assertEquals("HIDDEN", created.getPriceType());
        assertNull(created.getMinPrice());
        assertNull(created.getMaxPrice());
    }

    @Test
    void shouldDefaultToExactWhenPriceTypeNotSet() {
        CreatePgRequest request = buildBaseRequest("default-pricing-" + System.nanoTime() + "@test.com");
        // Do NOT set priceType

        PgResponse created = pgService.createPg(request);

        assertEquals("EXACT", created.getPriceType());
    }

    @Test
    void shouldRetainPricingFieldsOnFetch() {
        CreatePgRequest request = buildBaseRequest("fetch-pricing-" + System.nanoTime() + "@test.com");
        request.setPriceType(PriceType.RANGE);
        request.setMinPrice(new BigDecimal("6000"));
        request.setMaxPrice(new BigDecimal("9000"));
        PgResponse created = pgService.createPg(request);

        PgResponse fetched = pgService.getPgById(created.getId());

        assertEquals("RANGE", fetched.getPriceType());
        assertTrue(new BigDecimal("6000").compareTo(fetched.getMinPrice()) == 0,
            "minPrice should be 6000");
        assertTrue(new BigDecimal("9000").compareTo(fetched.getMaxPrice()) == 0,
            "maxPrice should be 9000");
    }

    @Test
    void shouldCreateMultiplePgsWithDifferentPricingStrategies() {
        // Create EXACT
        CreatePgRequest exactReq = buildBaseRequest("multi-exact-" + System.nanoTime() + "@test.com");
        exactReq.setPriceType(PriceType.EXACT);
        PgResponse exact = pgService.createPg(exactReq);

        // Create RANGE
        CreatePgRequest rangeReq = buildBaseRequest("multi-range-" + System.nanoTime() + "@test.com");
        rangeReq.setPriceType(PriceType.RANGE);
        rangeReq.setMinPrice(new BigDecimal("5000"));
        rangeReq.setMaxPrice(new BigDecimal("8000"));
        PgResponse range = pgService.createPg(rangeReq);

        // Create HIDDEN
        CreatePgRequest hiddenReq = buildBaseRequest("multi-hidden-" + System.nanoTime() + "@test.com");
        hiddenReq.setPriceType(PriceType.HIDDEN);
        PgResponse hidden = pgService.createPg(hiddenReq);

        // Verify each
        assertEquals("EXACT", pgService.getPgById(exact.getId()).getPriceType());
        assertEquals("RANGE", pgService.getPgById(range.getId()).getPriceType());
        assertEquals("HIDDEN", pgService.getPgById(hidden.getId()).getPriceType());
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private CreatePgRequest buildBaseRequest(String ownerEmail) {
        OwnerProfileRequest owner = new OwnerProfileRequest();
        owner.setFullName("Test Owner");
        owner.setEmail(ownerEmail);
        owner.setPhoneNumber("9876543210");

        SharingOptionRequest sharing = new SharingOptionRequest();
        sharing.setSharingType("Single");
        sharing.setRent(new BigDecimal("8000"));
        sharing.setAvailabilityCount(2);

        CreatePgRequest request = new CreatePgRequest();
        request.setOwner(owner);
        request.setName("Pricing Test PG");
        request.setDescription("Testing flexible pricing");
        request.setLatitude(12.9716);
        request.setLongitude(77.5946);
        request.setLocationLabel("Bangalore");
        request.setSharingOptions(List.of(sharing));
        return request;
    }
}
