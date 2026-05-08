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
import org.springframework.web.util.UriComponentsBuilder;
import sn.senannonces.annonceservice.dto.AnnonceRequest;
import sn.senannonces.annonceservice.dto.AnnonceResponse;
import sn.senannonces.annonceservice.dto.SubmitDecisionRequest;
import sn.senannonces.annonceservice.model.Annonce;
import sn.senannonces.annonceservice.service.AnnonceService;

import java.util.List;

/**
 * REST controller exposing the public {@code /annonces} API.
 *
 * <p>This controller is intentionally thin: it only translates between HTTP
 * concerns (status codes, URI, payload validation) and the
 * {@link AnnonceService} business layer.
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
            description = "Creates an ad with status EN_ATTENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ad created"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public ResponseEntity<AnnonceResponse> create(@Valid @RequestBody AnnonceRequest request) {
        Annonce created = annonceService.create(request);
        AnnonceResponse body = AnnonceResponse.from(created);
        return ResponseEntity.ok(body);
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
    @Operation(summary = "Submit a moderator decision",
            description = "Approves or rejects a pending ad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Decision applied"),
            @ApiResponse(responseCode = "404", description = "Ad not found"),
            @ApiResponse(responseCode = "409", description = "Invalid status transition")
    })
    public AnnonceResponse submit(@PathVariable Long id,
                                  @Valid @RequestBody SubmitDecisionRequest request) {
        return AnnonceResponse.from(annonceService.submitDecision(id, request.getDecision()));
    }


    @PatchMapping("/{id}/publier")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Publish an approved ad",
            description = "Transitions an APPROUVEE ad to PUBLIEE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ad published"),
            @ApiResponse(responseCode = "404", description = "Ad not found"),
            @ApiResponse(responseCode = "409", description = "Ad is not in APPROUVEE state")
    })
    public AnnonceResponse publish(@PathVariable Long id) {
        return AnnonceResponse.from(annonceService.publish(id));
    }
}
