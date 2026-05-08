package sn.senannonces.annonceservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body used to create a new ad")
public class AnnonceRequest {

    @NotBlank
    @Size(max = 200)
    @Schema(example = "Voiture à vendre", description = "Title of the ad")
    private String titre;

    @NotBlank
    @Size(max = 2000)
    @Schema(example = "Toyota Yaris", description = "Detailed description")
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(example = "2500000", description = "Asking price in XOF")
    private BigDecimal prix;

    @NotBlank
    @Size(max = 100)
    @Schema(example = "Dakar", description = "City where the item is located")
    private String ville;
}
