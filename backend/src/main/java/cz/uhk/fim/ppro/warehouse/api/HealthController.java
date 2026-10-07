package cz.uhk.fim.ppro.warehouse.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health-check controller – diagnostický endpoint pro infrastrukturu.
 *
 * <h2>Proč existuje, přestože frontend sám nevidí "server je down"?</h2>
 * <p>Pravda je, že pokud Spring Boot nespadne a vrátí HTML stránku, pak server
 * evidentně běží. Tento endpoint proto NENÍ určen pro zobrazení v prohlížeči,
 * ale slouží třem specifickým infrastrukturním účelům:</p>
 * <ol>
 *   <li><b>Docker HEALTHCHECK</b> – Docker daemon volá endpoint přímo a rozhoduje,
 *       zda kontejner restartovat. Viz {@code HEALTHCHECK} instrukce v Dockerfile.</li>
 *   <li><b>CI/CD pipeline</b> – automatický test po deployi:
 *       {@code curl -f http://server/api/v1/health || exit 1}.</li>
 *   <li><b>Stránka "Stav systému"</b> – zobrazuje verzi, profil a čas serveru;
 *       smysluplné metadata, ne jen "server žije".</li>
 * </ol>
 *
 * <p><b>Endpoint:</b> {@code GET /api/v1/health}</p>
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    /** Aktivní Spring profily (h2 / local / docker) – injektovány z prostředí. */
    private final Environment environment;

    /** Verze aplikace načtená z pom.xml přes Maven resource filtering. */
    @Value("${spring.application.name:drevenka-warehouse-backend}")
    private String appName;

    public HealthController(Environment environment) {
        this.environment = environment;
    }

    /**
     * Vrátí diagnostické informace o běžící aplikaci.
     *
     * <p>Odpověď obsahuje:
     * <ul>
     *   <li>{@code status}     – vždy "UP" (jinak by endpoint neodpovídal)</li>
     *   <li>{@code aplikace}   – název aplikace</li>
     *   <li>{@code verze}      – verze buildu</li>
     *   <li>{@code profily}    – aktivní Spring profily (h2 / local / docker)</li>
     *   <li>{@code serverTime} – aktuální čas serveru (pro ověření timezone)</li>
     * </ul>
     *
     * @return HTTP 200 s JSON payloadem diagnostických informací
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        // LinkedHashMap zachovává pořadí klíčů v JSON výstupu (čitelnější pro vývojáře).
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status",     "UP");
        payload.put("aplikace",   "Dřevěnka s.r.o. – Skladový systém");
        payload.put("appName",    appName);
        payload.put("verze",      "0.1.0-SNAPSHOT (Milestone 1)");
        // Aktivní profily – klíčové pro debugování "proč se připojuje na špatnou DB"
        payload.put("profily",    Arrays.asList(environment.getActiveProfiles()));
        payload.put("serverTime", LocalDateTime.now().toString());

        return ResponseEntity.ok(payload);
    }
}
