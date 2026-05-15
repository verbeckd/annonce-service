package sn.senannonces.annonceservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senannonces.annonceservice.client.ModerationClient;
import sn.senannonces.annonceservice.dto.AnnonceRequest;
import sn.senannonces.annonceservice.exception.AnnonceNotFoundException;
import sn.senannonces.annonceservice.exception.InvalidStatusTransitionException;
import sn.senannonces.annonceservice.model.Annonce;
import sn.senannonces.annonceservice.model.AnnonceStatus;
import sn.senannonces.annonceservice.repository.AnnonceRepository;

import java.util.List;

/**
 * Business logic for {@link Annonce} management.
 *
 * <p>This service is the single authority on lifecycle transitions.
 * Status is always stored here; moderation-service only sends decisions back via callback.
 */
@Service
public class AnnonceService {

    private final AnnonceRepository annonceRepository;
    private final ModerationClient moderationClient;

    public AnnonceService(AnnonceRepository annonceRepository, ModerationClient moderationClient) {
        this.annonceRepository = annonceRepository;
        this.moderationClient = moderationClient;
    }

    /**
     * Persists a new ad with the {@link AnnonceStatus#EN_ATTENTE} initial status.
     */
    @Transactional
    public Annonce create(AnnonceRequest request) {
        Annonce annonce = Annonce.builder()
                .titre(request.getTitre())
                .description(request.getDescription())
                .prix(request.getPrix())
                .ville(request.getVille())
                .statut(AnnonceStatus.EN_ATTENTE)
                .build();
        return annonceRepository.save(annonce);
    }

    /** Returns every ad currently stored. */
    @Transactional(readOnly = true)
    public List<Annonce> findAll() {
        return annonceRepository.findAll();
    }

    /**
     * Loads an ad by id.
     *
     * @throws AnnonceNotFoundException when no ad matches the id
     */
    @Transactional(readOnly = true)
    public Annonce findById(Long id) {
        return annonceRepository.findById(id)
                .orElseThrow(() -> new AnnonceNotFoundException(id));
    }

    /**
     * Submits a pending ad to moderation-service for review.
     *
     * <p>The ad must be in {@link AnnonceStatus#EN_ATTENTE}.
     * This method does NOT change the status — moderation-service will call back
     * via {@link #applyModerationResult} once a decision is made.
     *
     * @param id ad identifier
     * @return the annonce (still EN_ATTENTE at this point)
     * @throws AnnonceNotFoundException         when no ad matches the id
     * @throws InvalidStatusTransitionException when the ad is not EN_ATTENTE
     * @throws RuntimeException                 when moderation-service is unreachable
     */
    @Transactional(readOnly = true)
    public Annonce submitForModeration(Long id) {
        Annonce annonce = findById(id);
        if (annonce.getStatut() != AnnonceStatus.EN_ATTENTE) {
            throw new InvalidStatusTransitionException(
                    "Only EN_ATTENTE annonces can be submitted for moderation. Current status: "
                    + annonce.getStatut());
        }
        moderationClient.submitForModeration(annonce);
        return annonce;
    }

    /**
     * Applies a moderation decision received from moderation-service via callback.
     *
     * <ul>
     *   <li>{@link AnnonceStatus#APPROUVEE} → status becomes {@link AnnonceStatus#PUBLIEE}</li>
     *   <li>{@link AnnonceStatus#REJETEE}   → status becomes {@link AnnonceStatus#REJETEE}</li>
     * </ul>
     *
     * @param id       ad identifier
     * @param decision APPROUVEE or REJETEE (sent by moderation-service)
     * @return the updated ad
     * @throws AnnonceNotFoundException         when no ad matches the id
     * @throws InvalidStatusTransitionException when the decision is invalid or the ad is not EN_ATTENTE
     */
    @Transactional
    public Annonce applyModerationResult(Long id, AnnonceStatus decision) {
        if (decision != AnnonceStatus.APPROUVEE && decision != AnnonceStatus.REJETEE) {
            throw new InvalidStatusTransitionException(
                    "Moderation result must be APPROUVEE or REJETEE, got: " + decision);
        }
        Annonce annonce = findById(id);
        if (annonce.getStatut() != AnnonceStatus.EN_ATTENTE) {
            throw new InvalidStatusTransitionException(annonce.getStatut(), decision);
        }
        AnnonceStatus finalStatus = (decision == AnnonceStatus.APPROUVEE)
                ? AnnonceStatus.PUBLIEE
                : AnnonceStatus.REJETEE;
        annonce.setStatut(finalStatus);
        return annonceRepository.save(annonce);
    }

    /**
     * Publishes an approved ad (APPROUVEE → PUBLIEE).
     *
     * <p>This is a manual step kept for assignment compatibility.
     * In the main moderation flow, approval goes directly to PUBLIEE via callback.
     *
     * @throws AnnonceNotFoundException         when no ad matches the id
     * @throws InvalidStatusTransitionException when the ad is not APPROUVEE
     */
    @Transactional
    public Annonce publish(Long id) {
        Annonce annonce = findById(id);
        if (annonce.getStatut() != AnnonceStatus.APPROUVEE) {
            throw new InvalidStatusTransitionException(
                    annonce.getStatut(), AnnonceStatus.PUBLIEE);
        }
        annonce.setStatut(AnnonceStatus.PUBLIEE);
        return annonceRepository.save(annonce);
    }
}
