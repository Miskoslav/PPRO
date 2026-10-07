package cz.uhk.fim.ppro.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hlavní vstupní bod aplikace <b>Sklad Dřevěnka s.r.o.</b>.
 *
 * <p>Tato třída spouští Spring Boot kontejner a inicializuje celou třívrstvou aplikaci:
 * <ul>
 *   <li><b>Prezentační vrstva</b> – REST controllery a statické webové rozhraní</li>
 *   <li><b>Aplikační / byznys vrstva</b> – servisní třídy s obchodní logikou</li>
 *   <li><b>Datová vrstva</b> – JPA repozitáře + PostgreSQL (prod) / H2 (dev)</li>
 * </ul>
 *
 * <p><b>Aktivní profily:</b>
 * <ul>
 *   <li>{@code h2}    – lokální vývoj bez PostgreSQL (výchozí)</li>
 *   <li>{@code local} – lokální PostgreSQL na portu 5432</li>
 *   <li>{@code docker} – PostgreSQL v Docker síti (service name {@code db})</li>
 * </ul>
 *
 * <p><b>Projekt:</b> Semestrální projekt PPRO, FIM UHK, ZS 2026/2027<br>
 * <b>Zadání:</b> B – Sklad pro malý e-shop (Dřevěnka s.r.o.)
 *
 * @see <a href="http://localhost:8080/swagger-ui.html">Swagger UI (po spuštění)</a>
 */
@SpringBootApplication
public class WarehouseApplication {

    /**
     * Spouští Spring Boot aplikaci.
     * Spring automaticky načte konfiguraci z {@code application.yml},
     * aktivuje odpovídající profil a inicializuje všechny beany.
     *
     * @param args argumenty příkazové řádky (předávány do Spring kontextu)
     */
    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}
