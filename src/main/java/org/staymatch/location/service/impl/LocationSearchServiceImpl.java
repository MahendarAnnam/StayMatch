package org.staymatch.location.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.staymatch.common.model.City;
import org.staymatch.location.dto.LocationSuggestion;
import org.staymatch.location.dto.PlaceDetailsResponse;
import org.staymatch.location.repository.CityRepository;
import org.staymatch.location.service.LocationSearchService;

@Service
public class LocationSearchServiceImpl implements LocationSearchService {

    private static final Logger log = LoggerFactory.getLogger(LocationSearchServiceImpl.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private static final String AUTOCOMPLETE_URL = "https://places.googleapis.com/v1/places:autocomplete";
    private static final String PLACE_DETAILS_URL = "https://places.googleapis.com/v1/places/";

    // Default bias: center of India
    private static final double DEFAULT_BIAS_LAT = 20.5937;
    private static final double DEFAULT_BIAS_LNG = 78.9629;
    private static final double DEFAULT_BIAS_RADIUS = 25000.0; // 25 km — Google Places API max is 25000 meters

    private final CityRepository cityRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.google-maps.api-key:}")
    private String googleApiKey;

    public LocationSearchServiceImpl(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public List<LocationSuggestion> search(String query, int limit) {
        // Empty query → return all cities from DB (no API call needed)
        if (query == null || query.isBlank()) {
            return cityRepository.findAllByOrderByNameAsc().stream()
                .map(this::cityToSuggestion)
                .limit(limit)
                .collect(Collectors.toList());
        }

        // Try Google Places Autocomplete (New) first
        if (googleApiKey != null && !googleApiKey.isBlank()) {
            try {
                List<LocationSuggestion> googleResults = searchViaGooglePlaces(query, limit);
                if (!googleResults.isEmpty()) {
                    return googleResults;
                }
            } catch (Exception e) {
                log.warn("Google Places Autocomplete (New) failed for query '{}', falling back to DB search", query, e);
            }
        }

        // Fallback: search local city database
        return searchLocalCities(query, limit);
    }

    @Override
    public PlaceDetailsResponse getPlaceDetails(String placeId) {
        if (googleApiKey == null || googleApiKey.isBlank()) {
            throw new IllegalStateException("Google Maps API key not configured");
        }

        try {
            String url = PLACE_DETAILS_URL + placeId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Goog-Api-Key", googleApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Goog-FieldMask",
                "id,displayName,formattedAddress,shortFormattedAddress,location,addressComponents");

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            JsonNode root = JSON.readTree(response.getBody());
            return parseDetailsResult(root, placeId);
        } catch (Exception e) {
            log.error("Failed to get place details for placeId={}", placeId, e);
            throw new RuntimeException("Failed to resolve place details", e);
        }
    }

    // ── Google Places Autocomplete (New) ───────────────────────────

    private List<LocationSuggestion> searchViaGooglePlaces(String query, int limit) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Goog-Api-Key", googleApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build request body
        String requestBody = JSON.writeValueAsString(JSON.createObjectNode()
            .put("input", query)
            .set("locationBias", JSON.createObjectNode()
                .set("circle", JSON.createObjectNode()
                    .put("radius", DEFAULT_BIAS_RADIUS)
                    .set("center", JSON.createObjectNode()
                        .put("latitude", DEFAULT_BIAS_LAT)
                        .put("longitude", DEFAULT_BIAS_LNG)))));

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(AUTOCOMPLETE_URL, request, String.class);

        JsonNode root = JSON.readTree(response.getBody());
        JsonNode suggestions = root.path("suggestions");

        List<LocationSuggestion> results = new ArrayList<>();
        int count = 0;
        for (JsonNode suggestion : suggestions) {
            if (count >= limit) break;

            JsonNode placePrediction = suggestion.path("placePrediction");
            if (placePrediction.isMissingNode()) continue;

            LocationSuggestion s = new LocationSuggestion();
            s.setLabel(placePrediction.path("text").path("text").asText(""));
            s.setDescription(placePrediction.path("structuredFormat")
                .path("secondaryText").path("text").asText(""));
            s.setPlaceId(placePrediction.path("placeId").asText(null));
            s.setType(determineType(placePrediction));

            // lat/lng not available from autocomplete — resolved via getPlaceDetails
            s.setLat(null);
            s.setLng(null);

            results.add(s);
            count++;
        }

        return results;
    }

    private String determineType(JsonNode placePrediction) {
        JsonNode types = placePrediction.path("types");
        if (types.isArray()) {
            for (JsonNode t : types) {
                String type = t.asText();
                if ("locality".equals(type) || "administrative_area_level_2".equals(type)) {
                    return "city";
                }
            }
        }
        return "area";
    }

    // ── Place Details parsing (New API) ────────────────────────────

    private PlaceDetailsResponse parseDetailsResult(JsonNode root, String placeId) {
        PlaceDetailsResponse resp = new PlaceDetailsResponse();
        resp.setPlaceId(placeId);
        resp.setFullAddress(root.path("formattedAddress").asText(""));
        resp.setShortAddress(root.path("shortFormattedAddress").asText(""));
        resp.setLabel(root.path("displayName").path("text").asText(""));

        // location → lat/lng
        JsonNode location = root.path("location");
        resp.setLat(location.path("latitude").asDouble());
        resp.setLng(location.path("longitude").asDouble());

        // Parse addressComponents
        String sublocality = "";
        String locality = "";
        String city = "";
        String state = "";
        String pincode = "";

        for (JsonNode comp : root.path("addressComponents")) {
            String longText = comp.path("longText").asText("");
            JsonNode types = comp.path("types");
            for (JsonNode t : types) {
                String type = t.asText();
                switch (type) {
                    case "sublocality_level_1":
                    case "sublocality":
                        if (sublocality.isEmpty()) sublocality = longText;
                        break;
                    case "locality":
                        if (locality.isEmpty()) locality = longText;
                        break;
                    case "administrative_area_level_2":
                        if (city.isEmpty()) city = longText;
                        break;
                    case "administrative_area_level_1":
                        if (state.isEmpty()) state = longText;
                        break;
                    case "postal_code":
                        if (pincode.isEmpty()) pincode = longText;
                        break;
                }
            }
        }

        if (city.isEmpty()) city = locality;
        resp.setCity(city);
        resp.setArea(sublocality.isEmpty() ? null : sublocality);
        resp.setState(state);
        resp.setPincode(pincode);
        resp.setType(sublocality.isEmpty() ? "city" : "area");

        if (resp.getLabel().isEmpty()) {
            resp.setLabel(sublocality.isEmpty() ? city : sublocality + ", " + city);
        }

        return resp;
    }

    // ── Local DB fallback ──────────────────────────────────────────

    private List<LocationSuggestion> searchLocalCities(String query, int limit) {
        String lowerQuery = query.toLowerCase().trim();
        List<LocationSuggestion> suggestions = new ArrayList<>();

        List<City> cities = cityRepository.findAllByOrderByNameAsc();
        for (City city : cities) {
            if (city.getName().toLowerCase().contains(lowerQuery)) {
                suggestions.add(cityToSuggestion(city));
            }
            if (suggestions.size() >= limit) break;
        }

        return suggestions;
    }

    private LocationSuggestion cityToSuggestion(City city) {
        LocationSuggestion s = new LocationSuggestion(
            city.getName(),
            city.getName(),
            null,
            city.getLatitude(),
            city.getLongitude(),
            "city"
        );
        s.setDescription("India");
        return s;
    }
}


