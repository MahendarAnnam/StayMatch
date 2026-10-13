package org.staymatch.pgdiscovery;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.staymatch.pgdiscovery.dto.CreatePgRequest;
import org.staymatch.pgdiscovery.dto.PgResponse;
import org.staymatch.pgdiscovery.dto.UpdatePgRequest;
import org.staymatch.pgdiscovery.mapper.ImageMapper;
import org.staymatch.pgdiscovery.mapper.OwnerProfileMapper;
import org.staymatch.pgdiscovery.mapper.PgMapper;
import org.staymatch.pgdiscovery.mapper.SharingOptionMapper;
import org.staymatch.pgdiscovery.model.OwnerProfile;
import org.staymatch.pgdiscovery.model.Pg;
import org.staymatch.pgdiscovery.model.PriceType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for PgMapper — focused on pricing fields (priceType, minPrice, maxPrice).
 */
class PgMapperPricingTest {

    private PgMapper pgMapper;

    @BeforeEach
    void setUp() {
        pgMapper = new PgMapper(
            new OwnerProfileMapper(),
            new SharingOptionMapper(),
            new ImageMapper()
        );
    }

    // ── toEntity tests ─────────────────────────────────────────────────

    @Test
    void toEntity_shouldDefaultToExactWhenPriceTypeIsNull() {
        CreatePgRequest request = buildMinimalRequest();
        request.setPriceType(null);

        Pg pg = pgMapper.toEntity(request, buildOwner());

        assertEquals(PriceType.EXACT, pg.getPriceType());
        assertNull(pg.getMinPrice());
        assertNull(pg.getMaxPrice());
    }

    @Test
    void toEntity_shouldSetRangePriceType() {
        CreatePgRequest request = buildMinimalRequest();
        request.setPriceType(PriceType.RANGE);
        request.setMinPrice(new BigDecimal("4000"));
        request.setMaxPrice(new BigDecimal("7000"));

        Pg pg = pgMapper.toEntity(request, buildOwner());

        assertEquals(PriceType.RANGE, pg.getPriceType());
        assertEquals(new BigDecimal("4000"), pg.getMinPrice());
        assertEquals(new BigDecimal("7000"), pg.getMaxPrice());
    }

    @Test
    void toEntity_shouldSetHiddenPriceType() {
        CreatePgRequest request = buildMinimalRequest();
        request.setPriceType(PriceType.HIDDEN);

        Pg pg = pgMapper.toEntity(request, buildOwner());

        assertEquals(PriceType.HIDDEN, pg.getPriceType());
        assertNull(pg.getMinPrice());
        assertNull(pg.getMaxPrice());
    }

    @Test
    void toEntity_shouldSetExactPriceTypeExplicitly() {
        CreatePgRequest request = buildMinimalRequest();
        request.setPriceType(PriceType.EXACT);

        Pg pg = pgMapper.toEntity(request, buildOwner());

        assertEquals(PriceType.EXACT, pg.getPriceType());
    }

    // ── toResponse tests ───────────────────────────────────────────────

    @Test
    void toResponse_shouldMapExactPriceType() {
        Pg pg = buildMinimalPg();
        pg.setPriceType(PriceType.EXACT);

        PgResponse response = pgMapper.toResponse(pg);

        assertEquals("EXACT", response.getPriceType());
        assertNull(response.getMinPrice());
        assertNull(response.getMaxPrice());
    }

    @Test
    void toResponse_shouldMapRangePriceTypeWithPrices() {
        Pg pg = buildMinimalPg();
        pg.setPriceType(PriceType.RANGE);
        pg.setMinPrice(new BigDecimal("5000"));
        pg.setMaxPrice(new BigDecimal("8000"));

        PgResponse response = pgMapper.toResponse(pg);

        assertEquals("RANGE", response.getPriceType());
        assertEquals(new BigDecimal("5000"), response.getMinPrice());
        assertEquals(new BigDecimal("8000"), response.getMaxPrice());
    }

    @Test
    void toResponse_shouldMapHiddenPriceType() {
        Pg pg = buildMinimalPg();
        pg.setPriceType(PriceType.HIDDEN);

        PgResponse response = pgMapper.toResponse(pg);

        assertEquals("HIDDEN", response.getPriceType());
    }

    @Test
    void toResponse_shouldDefaultToExactWhenPriceTypeIsNull() {
        Pg pg = buildMinimalPg();
        pg.setPriceType(null);

        PgResponse response = pgMapper.toResponse(pg);

        assertEquals("EXACT", response.getPriceType());
    }

    // ── updateEntity tests ─────────────────────────────────────────────

    @Test
    void updateEntity_shouldUpdatePriceTypeToRange() {
        Pg pg = buildMinimalPg();
        pg.setPriceType(PriceType.EXACT);

        UpdatePgRequest update = new UpdatePgRequest();
        update.setPriceType(PriceType.RANGE);
        update.setMinPrice(new BigDecimal("3000"));
        update.setMaxPrice(new BigDecimal("6000"));

        pgMapper.updateEntity(pg, update);

        assertEquals(PriceType.RANGE, pg.getPriceType());
        assertEquals(new BigDecimal("3000"), pg.getMinPrice());
        assertEquals(new BigDecimal("6000"), pg.getMaxPrice());
    }

    @Test
    void updateEntity_shouldUpdatePriceTypeToHidden() {
        Pg pg = buildMinimalPg();
        pg.setPriceType(PriceType.EXACT);

        UpdatePgRequest update = new UpdatePgRequest();
        update.setPriceType(PriceType.HIDDEN);

        pgMapper.updateEntity(pg, update);

        assertEquals(PriceType.HIDDEN, pg.getPriceType());
    }

    @Test
    void updateEntity_shouldNotChangePriceTypeWhenNull() {
        Pg pg = buildMinimalPg();
        pg.setPriceType(PriceType.RANGE);
        pg.setMinPrice(new BigDecimal("4000"));

        UpdatePgRequest update = new UpdatePgRequest();
        update.setName("Updated Name");
        // priceType intentionally not set

        pgMapper.updateEntity(pg, update);

        assertEquals(PriceType.RANGE, pg.getPriceType());
        assertEquals(new BigDecimal("4000"), pg.getMinPrice());
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private CreatePgRequest buildMinimalRequest() {
        CreatePgRequest request = new CreatePgRequest();
        request.setName("Test PG");
        request.setLatitude(12.97);
        request.setLongitude(77.59);
        request.setLocationLabel("Test Location");
        return request;
    }

    private OwnerProfile buildOwner() {
        OwnerProfile owner = new OwnerProfile();
        owner.setFullName("Test Owner");
        owner.setEmail("owner@test.com");
        return owner;
    }

    private Pg buildMinimalPg() {
        Pg pg = new Pg();
        OwnerProfile owner = new OwnerProfile();
        owner.setFullName("Test Owner");
        owner.setEmail("owner@test.com");
        pg.setOwner(owner);
        pg.setName("Test PG");
        pg.setLatitude(12.97);
        pg.setLongitude(77.59);
        pg.setLocationLabel("Test Location");
        pg.setIsActive(true);
        return pg;
    }
}

