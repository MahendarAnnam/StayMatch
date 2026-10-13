package org.staymatch.common.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate-limiting filter using Bucket4j.
 *
 * <ul>
 *   <li>Limit: configurable requests per minute per client</li>
 *   <li>Key: userId from JWT (set by JwtAuthenticationFilter) → fallback to client IP for public endpoints</li>
 *   <li>Exclusion: /api/v1/auth/** is skipped entirely</li>
 *   <li>Response: HTTP 429 with consistent JSON error body and Retry-After header</li>
 * </ul>
 *
 * Must be registered <b>after</b> JwtAuthenticationFilter in the security filter chain
 * so that the "userId" request attribute is already populated when available.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final String AUTH_PATH_PREFIX = "/api/v1/auth/";
    private static final String H2_CONSOLE_PATH = "/h2-console";

    private final int requestsPerMinute;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(@Value("${app.rate-limit.requests-per-minute:50}") int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    // ── Skip auth endpoints and H2 console entirely ─────────────────────

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith(AUTH_PATH_PREFIX) || path.startsWith(H2_CONSOLE_PATH);
    }

    // ── Main filter logic ─────────────────────────────────────────────

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String key = resolveClientKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Allow — add informational headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            // Reject — 429
            long waitSeconds = Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds() + 1;
            log.warn("Rate limit exceeded for key={}, retryAfter={}s", key, waitSeconds);

            response.addHeader("Retry-After", String.valueOf(waitSeconds));
            response.addHeader("X-Rate-Limit-Remaining", "0");
            writeErrorResponse(response, waitSeconds);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────

    /**
     * Resolves the rate-limit key.
     * Prefers userId (set by JwtAuthenticationFilter) for authenticated requests;
     * falls back to client IP address for public / unauthenticated calls.
     */
    private String resolveClientKey(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return "user:" + userId;
        }

        // Fallback to IP — respect X-Forwarded-For if behind a reverse proxy
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank())
            ? forwarded.split(",")[0].trim()
            : request.getRemoteAddr();
        return "ip:" + ip;
    }

    /** Creates a new Bucket4j bucket with the configured rate. */
    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(requestsPerMinute)
            .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
            .build();
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Writes a 429 JSON error response matching the application's ErrorResponse structure.
     * Uses manual JSON construction to avoid Jackson version coupling.
     */
    private void writeErrorResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        int status = HttpStatus.TOO_MANY_REQUESTS.value();
        String json = """
            {
              "timestamp": "%s",
              "status": %d,
              "error": "%s",
              "message": "Rate limit exceeded. Please retry after %d seconds.",
              "validationErrors": null
            }
            """.formatted(Instant.now().toString(), status, HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(), retryAfterSeconds);

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(json);
    }
}

