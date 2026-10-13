package org.staymatch.pgdiscovery.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.staymatch.pgdiscovery.dto.CreatePgRequest;
import org.staymatch.pgdiscovery.dto.PgResponse;
import org.staymatch.pgdiscovery.dto.UpdatePgRequest;
import org.staymatch.pgdiscovery.model.FoodType;
import org.staymatch.pgdiscovery.model.OwnerProfile;
import org.staymatch.pgdiscovery.model.Pg;
import org.staymatch.pgdiscovery.model.PgFacility;
import org.staymatch.pgdiscovery.model.PriceType;

@Component
public class PgMapper {

    private static final Logger log = LoggerFactory.getLogger(PgMapper.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Map<String, List<String>>> MENU_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<String>> LIST_TYPE = new TypeReference<>() {};

    private final OwnerProfileMapper ownerProfileMapper;
    private final SharingOptionMapper sharingOptionMapper;
    private final ImageMapper imageMapper;

    public PgMapper(OwnerProfileMapper ownerProfileMapper,
                    SharingOptionMapper sharingOptionMapper,
                    ImageMapper imageMapper) {
        this.ownerProfileMapper = ownerProfileMapper;
        this.sharingOptionMapper = sharingOptionMapper;
        this.imageMapper = imageMapper;
    }

    public Pg toEntity(CreatePgRequest request, OwnerProfile owner) {
        Pg pg = new Pg();
        pg.setOwner(owner);
        pg.setName(request.getName());
        pg.setDescription(request.getDescription());
        pg.setLatitude(request.getLatitude());
        pg.setLongitude(request.getLongitude());
        pg.setLocationLabel(request.getLocationLabel());
        pg.setFullAddress(request.getFullAddress());
        pg.setShortAddress(request.getShortAddress());
        pg.setAddressCity(request.getAddressCity());
        pg.setAddressState(request.getAddressState());
        pg.setPincode(request.getPincode());
        pg.setIsActive(Boolean.TRUE);

        pg.setFoodType(request.getFoodType());
        pg.setWeeklyMenu(toJson(request.getWeeklyMenu()));
        pg.setNonVegDays(toJson(request.getNonVegDays()));
        pg.setEggDays(toJson(request.getEggDays()));

        pg.setPriceType(request.getPriceType() != null ? request.getPriceType() : PriceType.EXACT);
        pg.setMinPrice(request.getMinPrice());
        pg.setMaxPrice(request.getMaxPrice());

        if (request.getSharingOptions() != null) {
            request.getSharingOptions().forEach(optReq -> {
                pg.addSharingOption(sharingOptionMapper.toEntity(optReq));
            });
        }

        if (request.getImages() != null) {
            request.getImages().forEach(imgReq -> {
                pg.addImage(imageMapper.toEntity(imgReq));
            });
        }

        return pg;
    }

    public PgResponse toResponse(Pg pg) {
        PgResponse response = new PgResponse();
        response.setId(pg.getId());
        response.setOwner(ownerProfileMapper.toResponse(pg.getOwner()));
        response.setName(pg.getName());
        response.setDescription(pg.getDescription());
        response.setLatitude(pg.getLatitude());
        response.setLongitude(pg.getLongitude());
        response.setLocationLabel(pg.getLocationLabel());
        response.setFullAddress(pg.getFullAddress());
        response.setShortAddress(pg.getShortAddress());
        response.setAddressCity(pg.getAddressCity());
        response.setAddressState(pg.getAddressState());
        response.setPincode(pg.getPincode());
        response.setIsActive(pg.getIsActive());
        response.setCreatedAt(pg.getCreatedAt());
        response.setUpdatedAt(pg.getUpdatedAt());

        // Food fields
        response.setFoodType(pg.getFoodType() != null ? pg.getFoodType().name() : null);
        response.setWeeklyMenu(fromJson(pg.getWeeklyMenu(), MENU_TYPE));
        response.setNonVegDays(fromJson(pg.getNonVegDays(), LIST_TYPE));
        response.setEggDays(fromJson(pg.getEggDays(), LIST_TYPE));

        // Pricing fields
        response.setPriceType(pg.getPriceType() != null ? pg.getPriceType().name() : PriceType.EXACT.name());
        response.setMinPrice(pg.getMinPrice());
        response.setMaxPrice(pg.getMaxPrice());

        if (pg.getSharingOptions() != null) {
            response.setSharingOptions(
                pg.getSharingOptions().stream()
                    .map(sharingOptionMapper::toResponse)
                    .collect(Collectors.toList())
            );
        }

        if (pg.getPgFacilities() != null) {
            List<String> facilityNames = pg.getPgFacilities().stream()
                .map(PgFacility::getFacility)
                .map(f -> f.getFacilityName())
                .collect(Collectors.toList());
            response.setFacilities(facilityNames);
        } else {
            response.setFacilities(Collections.emptyList());
        }

        if (pg.getImages() != null) {
            response.setImages(
                pg.getImages().stream()
                    .map(imageMapper::toResponse)
                    .collect(Collectors.toList())
            );
        }

        return response;
    }

    public void updateEntity(Pg pg, UpdatePgRequest request) {
        if (request.getName() != null) {
            pg.setName(request.getName());
        }
        if (request.getDescription() != null) {
            pg.setDescription(request.getDescription());
        }
        if (request.getLatitude() != null) {
            pg.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            pg.setLongitude(request.getLongitude());
        }
        if (request.getLocationLabel() != null) {
            pg.setLocationLabel(request.getLocationLabel());
        }
        if (request.getFullAddress() != null) {
            pg.setFullAddress(request.getFullAddress());
        }
        if (request.getShortAddress() != null) {
            pg.setShortAddress(request.getShortAddress());
        }
        if (request.getAddressCity() != null) {
            pg.setAddressCity(request.getAddressCity());
        }
        if (request.getAddressState() != null) {
            pg.setAddressState(request.getAddressState());
        }
        if (request.getPincode() != null) {
            pg.setPincode(request.getPincode());
        }
        if (request.getIsActive() != null) {
            pg.setIsActive(request.getIsActive());
        }

        if (request.getFoodType() != null) {
            pg.setFoodType(request.getFoodType());
        }
        if (request.getWeeklyMenu() != null) {
            pg.setWeeklyMenu(toJson(request.getWeeklyMenu()));
        }
        if (request.getNonVegDays() != null) {
            pg.setNonVegDays(toJson(request.getNonVegDays()));
        }
        if (request.getEggDays() != null) {
            pg.setEggDays(toJson(request.getEggDays()));
        }

        if (request.getPriceType() != null) {
            pg.setPriceType(request.getPriceType());
        }
        if (request.getMinPrice() != null) {
            pg.setMinPrice(request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            pg.setMaxPrice(request.getMaxPrice());
        }

        if (request.getSharingOptions() != null) {
            pg.getSharingOptions().clear();
            request.getSharingOptions().forEach(optReq ->
                pg.addSharingOption(sharingOptionMapper.toEntity(optReq))
            );
        }

        if (request.getImages() != null) {
            pg.getImages().clear();
            request.getImages().forEach(imgReq ->
                pg.addImage(imageMapper.toEntity(imgReq))
            );
        }
    }

    // --- JSON helpers ---

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return JSON.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize value to JSON", e);
            return null;
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return JSON.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize JSON: {}", json, e);
            return null;
        }
    }
}

