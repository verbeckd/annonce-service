package sn.senannonces.annonceservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sn.senannonces.annonceservice.dto.AnnonceRequest;
import sn.senannonces.annonceservice.dto.AnnonceResponse;
import sn.senannonces.annonceservice.dto.SubmitDecisionRequest;
import sn.senannonces.annonceservice.service.AnnonceService;

import java.util.List;

/**
 * REST controller exposing the {@code /annonces} API.
 *
 * <p>Thin controller: translates HTTP ↔ DTOs and delegates all business logic
 * to {@link AnnonceService}.
 */
@RestController
@RequestMapping("/annonces")
@Tag(name = "Annonces", description = "Manage classified ads (CRUD and lifecycle)")
public class AnnonceController {

    private final AnnonceService annonceService;

    public AnnonceController(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }

    @PostMapping
    @Operation(summary = "Create a new ad",
            description = "Creates an ad with status EN_ATTENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ad created"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public ResponseEntity<AnnonceResponse> create(@Valid @RequestBody AnnonceRequest request) {
        return ResponseEntity.ok(AnnonceResponse.from(annonceService.create(request)));
    }

    @GetMapping
    @Operation(summary = "List all ads")
    public List<AnnonceResponse> list() {
        return annonceService.findAll().stream()
                .map(AnnonceResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an ad by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ad found"),
            @ApiResponse(responseCode = "404", description = "Ad not found")
    })
    public AnnonceResponse get(@PathVariable Long id) {
        return AnnonceResponse.from(annonceService.findById(id));
    }

    @PostMapping("/{id}/soumettre")
    @Operation(
            summary = "Submit an ad for moderation",
            description = """
                    Sends the ad to moderation-service for review. \
                    No body required — do NOT send a decision here. \
                    The status stays EN_ATTENTE until a moderator approves or rejects via \
                    PATCH /moderations/{id}/approve or /reject on moderation-service."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ad submitted to moderation-service"),
            @ApiResponse(responseCode = "404", description = "Ad not found"),
            @ApiResponse(responseCode = "409", description = "Ad is not in EN_ATTENTE state"),
            @ApiResponse(responseCode = "500", description = "moderation-service unreachable")
    })
    public AnnonceResponse submit(@PathVariable Long id) {
        return AnnonceResponse.from(annonceService.submitForModeration(id));
    }

    @PatchMapping("/{id}/statut")
    @Operation(
            summary = "Internal callback — receive moderation result",
            description = """
                    Called by moderation-service only after a moderation decision. \
                    Send APPROUVEE to publish the ad (status → PUBLIEE) \
                    or REJETEE to reject it (status → REJETEE)."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid decision"),
            @ApiResponse(responseCode = "404", description = "Ad not found"),
            @ApiResponse(responseCode = "409", description = "Invalid status transition")
    })
    public AnnonceResponse applyModerationResult(
            @PathVariable Long id,
            @Valid @RequestBody SubmitDecisionRequest request) {
        return AnnonceResponse.from(annonceService.applyModerationResult(id, request.getDecision()));
    }

    @PatchMapping("/{id}/publier")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Publish an approved ad",
            description = "Transitions an APPROUVEE ad to PUBLIEE (manual step, kept for assignment compatibility)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ad published"),
            @ApiResponse(responseCode = "404", description = "Ad not found"),
            @ApiResponse(responseCode = "409", description = "Ad is not in APPROUVEE state")
    })
    public AnnonceResponse publish(@PathVariable Long id) {
        return AnnonceResponse.from(annonceService.publish(id));
    }
}
