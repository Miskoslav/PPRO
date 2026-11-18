package cz.uhk.fim.ppro.warehouse.repository;

import cz.uhk.fim.ppro.warehouse.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repozitář pro položky zákaznických objednávek.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Načte všechny položky pro danou objednávku včetně produktů a skladů.
     * Používá JOIN FETCH pro eliminaci N+1 problémů.
     */
    @Query("""
        SELECT oi FROM OrderItem oi
        JOIN FETCH oi.product
        LEFT JOIN FETCH oi.warehouse
        WHERE oi.order.id = :orderId
        """)
    List<OrderItem> findByOrderIdWithDetails(Long orderId);
}
