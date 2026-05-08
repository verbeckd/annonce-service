package sn.senannonces.annonceservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.senannonces.annonceservice.model.Annonce;

/**
 * Spring Data JPA repository for the {@link Annonce} entity.
 *
 * <p>Provides standard CRUD operations through {@link JpaRepository}.
 */
@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long> {
}
