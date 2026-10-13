package org.staymatch.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Helper bean for ownership validation — ensures users can only modify their own resources.
 * Used via SpEL in @PreAuthorize expressions.
 */
@Component("ownershipGuard")
public class OwnershipGuard {

    /**
     * Returns the authenticated user's ID from the JWT-populated request attribute.
     */
    public Long getAuthenticatedUserId() {
        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new AccessDeniedException("No request context available");
        }
        Object userId = attrs.getRequest().getAttribute("userId");
        if (userId == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        return (Long) userId;
    }
}

