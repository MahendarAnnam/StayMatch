package org.staymatch.location.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.staymatch.location.dto.StructuredAddressResponse;
import org.staymatch.location.service.GeocodingService;

/**
 * Reverse-geocoding implementation.
 * <p>
 * Primary: Google Maps Geocoding API (if api-key configured).
 * Fallback: OpenStreetMap Nominatim (free, no key required).
 */
@Service
public class GeocodingServiceImpl implements GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingServiceImpl.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.google-maps.api-key:}")
    private String googleApiKey;

    @Override
    public StructuredAddressResponse reverseGeocode(double lat, double lng) {
        // Try Google first if key is configured
        if (googleApiKey != null && !googleApiKey.isBlank()) {
            try {
                return reverseGeocodeGoogle(lat, lng);
            } catch (Exception e) {
                log.warn("Google geocoding failed for ({}, {}), falling back to Nominatim", lat, lng, e);
            }
        }

        // Fallback: Nominatim
        try {
            return reverseGeocodeNominatim(lat, lng);
        } catch (Exception e) {
            log.warn("Nominatim geocoding failed for ({}, {}), returning coordinate fallback", lat, lng, e);
        }

        // Ultimate fallback
        return coordinateFallback(lat, lng);
    }

    // ── Google Maps Geocoding ────────────────────────────────────────

    private StructuredAddressResponse reverseGeocodeGoogle(double lat, double lng) throws Exception {
        String url = String.format(
                "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s&language=en",
                lat, lng, googleApiKey);

        String body = restTemplate.getForObject(url, String.class);
        JsonNode root = JSON.readTree(body);

        if (!"OK".equals(root.path("status").asText())) {
            throw new RuntimeException("Google API status: " + root.path("status").asText());
        }

        JsonNode results = root.path("results");
        if (!results.isArray() || results.isEmpty()) {
            throw new RuntimeException("No results from Google API");
        }

        JsonNode first = results.get(0);
        String fullAddress = first.path("formatted_address").asText("");

        // Parse address_components
        String sublocality = "";
        String locality = "";
        String city = "";
        String state = "";
        String pincode = "";

        for (JsonNode comp : first.path("address_components")) {
            JsonNode types = comp.path("types");
            String longName = comp.path("long_name").asText("");
            for (JsonNode t : types) {
                String type = t.asText();
                switch (type) {
                    case "sublocality_level_1":
                    case "sublocality":
                        if (sublocality.isEmpty()) sublocality = longName;
                        break;
                    case "locality":
                        if (locality.isEmpty()) locality = longName;
                        break;
                    case "administrative_area_level_2":
                        if (city.isEmpty()) city = longName;
                        break;
                    case "administrative_area_level_1":
                        if (state.isEmpty()) state = longName;
                        break;
                    case "postal_code":
                        if (pincode.isEmpty()) pincode = longName;
                        break;
                }
            }
        }

        // City defaults to locality if not set
        if (city.isEmpty()) city = locality;

        // Build short address: "Kondapur, Hyderabad"
        String shortAddress = buildShortAddress(sublocality, city);

        return new StructuredAddressResponse(fullAddress, shortAddress, city, state, pincode, lat, lng);
    }

    // ── Nominatim (OSM) ─────────────────────────────────────────────

    private StructuredAddressResponse reverseGeocodeNominatim(double lat, double lng) throws Exception {
        String url = String.format(
                "https://nominatim.openstreetmap.org/reverse?lat=%f&lon=%f&format=json&addressdetails=1&accept-language=en",
                lat, lng);

        String body = restTemplate.getForObject(url, String.class);
        JsonNode root = JSON.readTree(body);
        JsonNode addr = root.path("address");

        String area = firstNonEmpty(
                addr.path("suburb").asText(""),
                addr.path("neighbourhood").asText(""),
                addr.path("village").asText("")
        );
        String city = firstNonEmpty(
                addr.path("city").asText(""),
                addr.path("town").asText(""),
                addr.path("county").asText(""),
                addr.path("state_district").asText("")
        );
        String state = addr.path("state").asText("");
        String pincode = addr.path("postcode").asText("");
        String fullAddress = root.path("display_name").asText("");

        String shortAddress = buildShortAddress(area, city);

        return new StructuredAddressResponse(fullAddress, shortAddress, city, state, pincode, lat, lng);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private StructuredAddressResponse coordinateFallback(double lat, double lng) {
        String label = String.format("%.4f, %.4f", lat, lng);
        return new StructuredAddressResponse(label, label, "", "", "", lat, lng);
    }

    private String buildShortAddress(String area, String city) {
        StringBuilder sb = new StringBuilder();
        if (!area.isEmpty()) sb.append(area);
        if (!city.isEmpty() && !city.equals(area)) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(city);
        }
        return !sb.isEmpty() ? sb.toString() : "";
    }

    private String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.isEmpty()) return v;
        }
        return "";
    }
}

