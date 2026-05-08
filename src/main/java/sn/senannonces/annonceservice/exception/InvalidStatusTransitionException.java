package sn.senannonces.annonceservice.exception;

import sn.senannonces.annonceservice.model.AnnonceStatus;

/**
 * Thrown when a requested status change violates the lifecycle rules
 * defined in {@link AnnonceStatus}.
 *
 * <p>Mapped to HTTP 409 (Conflict) by the global exception handler.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    /**
     * Builds the exception with a message describing the rejected transition.
     *
     * @param current current status of the ad
     * @param target  status that was requested
     */
    public InvalidStatusTransitionException(AnnonceStatus current, AnnonceStatus target) {
        super("Cannot transition from " + current + " to " + target);
    }

    /**
     * Builds the exception with a custom message.
     *
     * @param message human-readable explanation
     */
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
