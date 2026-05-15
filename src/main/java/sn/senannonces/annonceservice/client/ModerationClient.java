package sn.senannonces.annonceservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import sn.senannonces.annonceservice.model.Annonce;

import java.util.Map;

/**
 * HTTP client for communicating with moderation-service.
 *
 * <p>Calls {@code POST /moderations/submit} to hand off a pending annonce for review.
 */
@Component
public class ModerationClient {

    private final RestTemplate restTemplate;

    @Value("${moderation.service.base-url}")
    private String moderationBaseUrl;

    public ModerationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends an annonce to moderation-service for review.
     *
     * @param annonce the annonce to submit (must be in EN_ATTENTE)
     * @throws RuntimeException if moderation-service is unreachable
     */
    public void submitForModeration(Annonce annonce) {
        String url = moderationBaseUrl + "/moderations/submit";
        Map<String, Object> body = Map.of(
                "annonceId", annonce.getId(),
                "titre", annonce.getTitre(),
                "description", annonce.getDescription(),
                "prix", annonce.getPrix(),
                "ville", annonce.getVille()
        );
        try {
            restTemplate.postForObject(url, body, Map.class);
        } catch (RestClientException e) {
            throw new RuntimeException(
                    "Could not reach moderation-service at " + url + ": " + e.getMessage(), e);
        }
    }
}
