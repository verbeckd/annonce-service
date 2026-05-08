package sn.senannonces.annonceservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senannonces.annonceservice.model.AnnonceStatus;

/**
 * Body of the moderation endpoint {@code POST /annonces/{id}/soumettre}.
 *
 * <p>Only {@link AnnonceStatus#APPROUVEE} or {@link AnnonceStatus#REJETEE}
 * are accepted as decisions; any other value is rejected by the service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Moderator decision on a pending ad")
public class SubmitDecisionRequest {

    @NotNull
    @Schema(example = "APPROUVEE",
            description = "Either APPROUVEE or REJETEE",
            allowableValues = {"APPROUVEE", "REJETEE"})
    private AnnonceStatus decision;
}
