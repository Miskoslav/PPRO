package cz.uhk.fim.ppro.warehouse.repository;

import cz.uhk.fim.ppro.warehouse.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repozitář pro perzistentní operace nad entitou {@link Product}.
 * <p>
 * Zajišťuje bezpečný a typový přístup k databázové tabulce {@code products}
 * bez nutnosti psaní manuálního SQL. Tvoří perzistentní vrstvu v souladu
 * s pravidly třívrstvé architektury PPRO.
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Vyhledá produkt podle unikátního skladového kódu (SKU).
     *
     * @param sku unikátní kód hračky (např. "HRA-KAC-001")
     * @return {@link Optional} obsahující nalezený produkt, nebo prázdný, pokud neexistuje
     */
    Optional<Product> findBySku(String sku);

    /**
     * Ověří existenci produktu se zadaným SKU kódem.
     * Používá se při validaci vytváření nového produktu pro zamezení kolizí.
     *
     * @param sku unikátní kód hračky
     * @return {@code true}, pokud již produkt s tímto SKU v databázi existuje
     */
    boolean existsBySku(String sku);

    /**
     * Vrátí seznam všech aktivních produktů nabízených k prodeji, řazených abecedně podle názvu.
     *
     * @return seznam aktivních hraček
     */
    List<Product> findByIsActiveTrueOrderByNameAsc();

    /**
     * Vrátí aktivní produkty, které mají nastavenou minimální bezpečnou zásobu vyšší než nula.
     * V Milestonu 1 slouží pro demonstraci vyhodnocení položek vyžadujících sledování stavu.
     *
     * @return seznam sledovaných produktů
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.minStockLevel > 0 ORDER BY p.minStockLevel DESC")
    List<Product> findActiveWithMinimumStockConfigured();
}

