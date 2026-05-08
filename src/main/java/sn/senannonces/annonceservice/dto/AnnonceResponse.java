package sn.senannonces.annonceservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senannonces.annonceservice.model.Annonce;
import sn.senannonces.annonceservice.model.AnnonceStatus;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Public representation of an ad")
public class AnnonceResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Voiture à vendre")
    private String titre;

    @Schema(example = "Toyota Yaris")
    private String description;

    @Schema(example = "2500000")
    private BigDecimal prix;

    @Schema(example = "Dakar")
    private String ville;

    @Schema(example = "EN_ATTENTE")
    private AnnonceStatus statut;

    /**
     * Maps an {@link Annonce} entity to its API response counterpart.
     *
     * @param annonce entity to map (must not be {@code null})
     * @return a populated {@link AnnonceResponse}
     */
    public static AnnonceResponse from(Annonce annonce) {
        return AnnonceResponse.builder()
                .id(annonce.getId())
                .titre(annonce.getTitre())
                .description(annonce.getDescription())
                .prix(annonce.getPrix())
                .ville(annonce.getVille())
                .statut(annonce.getStatut())
                .build();
    }
}
