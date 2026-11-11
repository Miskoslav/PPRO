package cz.uhk.fim.ppro.warehouse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfigurace specifikace OpenAPI 3 a interaktivní dokumentace Swagger UI.
 * Dokumentace je dostupná na cestě {@code /swagger-ui.html}.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI warehouseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dřevěnka s.r.o. – Skladový systém API")
                        .description("REST API skladového systému pro malý e-shop s dřevěnými hračkami. " +
                                "Semestrální projekt předmětu Pokročilé programování (PPRO), FIM UHK (ZS 2026/2027).")
                        .version("0.1.0")
                        .contact(new Contact()
                                .name("Dřevěnka s.r.o. Vývojový tým")
                                .url("https://github.com/Miskoslav/PPRO")));
    }
}

