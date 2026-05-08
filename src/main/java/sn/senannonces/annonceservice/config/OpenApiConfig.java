package sn.senannonces.annonceservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for the annonce-service.
 *
 * <p>The Swagger UI is exposed at {@code /swagger-ui.html} and the raw
 * OpenAPI document at {@code /v3/api-docs}.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Builds the top-level OpenAPI document description.
     *
     * @return the configured {@link OpenAPI} bean
     */
    @Bean
    public OpenAPI annonceServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Annonce Service API")
                        .description("REST API for managing classified ads on SenAnnonces")
                        .version("1.0.0")
                        .contact(new Contact().name("SenAnnonces"))
                        .license(new License().name("MIT")));
    }
}
