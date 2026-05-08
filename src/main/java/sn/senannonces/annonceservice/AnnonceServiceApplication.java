package sn.senannonces.annonceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Annonce microservice.
 *
 * <p>This Spring Boot application exposes a REST API to manage classified ads
 * (annonces) for the SenAnnonces platform: creation, listing, retrieval,
 * moderation submission and publication.
 */
@SpringBootApplication
public class AnnonceServiceApplication {

    /**
     * Bootstraps the Spring application context.
     *
     * @param args command line arguments forwarded to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(AnnonceServiceApplication.class, args);
    }
}
