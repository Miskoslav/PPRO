package cz.uhk.fim.ppro.warehouse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Základní smoke-test: ověřuje, že se Spring kontejner správně načte.
 * Používá H2 in-memory profil – nevyžaduje běžící PostgreSQL.
 */
@SpringBootTest
@ActiveProfiles("h2")
class WarehouseApplicationTests {

    @Test
    void contextLoads() {
        // Smoke test – pokud Spring context naběhne bez chyby, je vše v pořádku.
    }

}
