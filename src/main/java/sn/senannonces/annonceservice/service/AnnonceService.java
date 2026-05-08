package sn.senannonces.annonceservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
 * <p>This service is the single authority on lifecycle transitions: any
 * unsupported transition raises an {@link InvalidStatusTransitionException}.
 */
@Service
public class AnnonceService {

    private final AnnonceRepository annonceRepository;

    /**
     * Creates the service with its persistence dependency.
     *
     * @param annonceRepository repository used for persistence
     */
    public AnnonceService(AnnonceRepository annonceRepository) {
        this.annonceRepository = annonceRepository;
    }

    /**
     * Persists a new ad with the {@link AnnonceStatus#EN_ATTENTE} initial status.
     *
     * @param request validated payload
     * @return the persisted ad with its generated id
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

    /**
     * Returns every ad currently stored.
     *
     * @return all ads, in repository order (no pagination in this MVP)
     */
    @Transactional(readOnly = true)
    public List<Annonce> findAll() {
        return annonceRepository.findAll();
    }

    /**
     * Loads an ad by id.
     *
     * @param id ad identifier
     * @return the matching ad
     * @throws AnnonceNotFoundException when no ad matches the id
     */
    @Transactional(readOnly = true)
    public Annonce findById(Long id) {
        return annonceRepository.findById(id)
                .orElseThrow(() -> new AnnonceNotFoundException(id));
    }

    /**
     * Applies a moderator decision on a pending ad.
     *
     * <p>The current status must be {@link AnnonceStatus#EN_ATTENTE} and the
     * decision must be either {@link AnnonceStatus#APPROUVEE} or
     * {@link AnnonceStatus#REJETEE}.
     *
     * @param id       ad identifier
     * @param decision moderator outcome
     * @return the updated ad
     * @throws AnnonceNotFoundException         when no ad matches the id
     * @throws InvalidStatusTransitionException when the ad is not pending or
     *                                          the decision is not allowed
     */
    @Transactional
    public Annonce submitDecision(Long id, AnnonceStatus decision) {
        if (decision != AnnonceStatus.APPROUVEE && decision != AnnonceStatus.REJETEE) {
            throw new InvalidStatusTransitionException(
                    "Decision must be APPROUVEE or REJETEE, got: " + decision);
        }
        Annonce annonce = findById(id);
        if (annonce.getStatut() != AnnonceStatus.EN_ATTENTE) {
            throw new InvalidStatusTransitionException(annonce.getStatut(), decision);
        }
        annonce.setStatut(decision);
        return annonceRepository.save(annonce);
    }

    /**
     * Publishes an approved ad, switching its status to
     * {@link AnnonceStatus#PUBLIEE}.
     *
     * @param id ad identifier
     * @return the updated ad
     * @throws AnnonceNotFoundException         when no ad matches the id
     * @throws InvalidStatusTransitionException when the ad is not approved
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
