package org.staymatch.common.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.staymatch.auth.exception.RefreshTokenExpiredException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ...existing code...

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", null);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenExpired(RefreshTokenExpiredException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        return buildError(HttpStatus.BAD_REQUEST, "Validation failed", validationErrors);
    }

    /**
     * Handles @Validated constraint violations on path variables and request parameters
     * (e.g., @Min(1) on controller method params). Returns 400 with per-field messages.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String field = extractFieldName(violation.getPropertyPath().toString());
            validationErrors.put(field, violation.getMessage());
        });

        return buildError(HttpStatus.BAD_REQUEST, "Constraint violation", validationErrors);
    }

    /**
     * Handles unique-constraint and FK-constraint violations from the database layer.
     * Returns 409 with a safe, non-leaking message.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String safeMessage = "A data integrity constraint was violated. "
            + "This is typically caused by a duplicate entry or an invalid reference.";
        return buildError(HttpStatus.CONFLICT, safeMessage, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", null);
    }

    // --- Private helpers ---

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message, Map<String, String> validationErrors) {
        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            validationErrors
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Extracts the leaf field name from a property path like "getPgById.id" → "id".
     */
    private String extractFieldName(String propertyPath) {
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }
}

