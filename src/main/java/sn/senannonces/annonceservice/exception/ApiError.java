package sn.senannonces.annonceservice.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Uniform error payload returned for every handled exception.
 */
@Getter
@Builder
@AllArgsConstructor
public class ApiError {

    /** ISO-8601 timestamp produced server-side. */
    private final Instant timestamp;

    /** HTTP status code. */
    private final int status;

    /** Short error label, typically the HTTP reason phrase. */
    private final String error;

    /** Human-readable explanation of the error. */
    private final String message;

    /** Optional list of field-level validation errors. */
    private final List<String> details;
}
