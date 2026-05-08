package sn.senannonces.annonceservice.exception;

/**
 * Thrown when an ad lookup by id does not match any persisted record.
 *
 * <p>Mapped to HTTP 404 by the global exception handler.
 */
public class AnnonceNotFoundException extends RuntimeException {

    /**
     * Builds a not-found exception with a default message including the missing id.
     *
     * @param id identifier that was searched for
     */
    public AnnonceNotFoundException(Long id) {
        super("Annonce not found with id: " + id);
    }
}
