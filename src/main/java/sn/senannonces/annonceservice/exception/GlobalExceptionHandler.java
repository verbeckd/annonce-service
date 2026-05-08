package sn.senannonces.annonceservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Centralised translation of exceptions into HTTP responses.
 *
 * <p>Keeping this logic in a single place ensures all controllers return a
 * consistent error envelope ({@link ApiError}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles missing-resource lookups and returns HTTP 404.
     *
     * @param ex thrown not-found exception
     * @return uniform error payload
     */
    @ExceptionHandler(AnnonceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(AnnonceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /**
     * Handles illegal lifecycle transitions and returns HTTP 409.
     *
     * @param ex thrown transition exception
     * @return uniform error payload
     */
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiError> handleInvalidTransition(InvalidStatusTransitionException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /**
     * Handles bean-validation failures and returns HTTP 400 with the list of
     * field errors.
     *
     * @param ex validation exception raised by Spring
     * @return uniform error payload with per-field details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    /**
     * Handles malformed JSON or unreadable payloads and returns HTTP 400.
     *
     * @param ex thrown deserialization exception
     * @return uniform error payload
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request", null);
    }

    /**
     * Catch-all for any unexpected exception, returns HTTP 500.
     *
     * @param ex unexpected runtime exception
     * @return uniform error payload
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAny(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error: " + ex.getMessage(), null);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, List<String> details) {
        ApiError body = ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .details(details)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
