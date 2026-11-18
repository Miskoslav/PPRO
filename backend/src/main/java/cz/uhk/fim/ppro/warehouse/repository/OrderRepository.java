package cz.uhk.fim.ppro.warehouse.repository;

import cz.uhk.fim.ppro.warehouse.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repozitář pro zákaznické objednávky.
 *
 * <p>
 * Objednávky jsou jádrem kritického bodu zadání B: rezervační mechanismus
 * zabraňující prodeji neexistujícího zboží. Tento repozitář poskytuje
 * optimalizované dotazy s předčítáním zákazníka a položek, aby se zabránilo
 * N+1 problémům při zobrazení přehledu objednávek.
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Načte všechny objednávky seřazené od nejnovější, s předčítáním zákazníka.
     * Vhodné pro zobrazení přehledového seznamu objednávek ve webovém rozhraní.
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.customer ORDER BY o.createdAt DESC")
    List<Order> findAllWithCustomerOrderByCreatedAtDesc();

    /**
     * Načte jednu objednávku včetně zákazníka a všech položek s produkty a sklady.
     * Efektivní načtení kompletního detailu objednávky bez N+1 problémů.
     */
    @Query("""
        SELECT DISTINCT o FROM Order o
        JOIN FETCH o.customer
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.product
        LEFT JOIN FETCH i.warehouse
        WHERE o.id = :id
        """)
    Optional<Order> findByIdWithDetails(Long id);

    /**
     * Vyhledá objednávku podle čitelného čísla objednávky (ORD-2026-XXXXX).
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Generuje pořadové číslo pro novou objednávku.
     * Počítá všechny objednávky v daném roce a přičte 1.
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE YEAR(o.createdAt) = :year")
    long countByYear(int year);
}
