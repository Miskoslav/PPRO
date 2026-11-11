package cz.uhk.fim.ppro.warehouse.repository;

import cz.uhk.fim.ppro.warehouse.model.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repozitář pro entitu stavu skladové zásoby {@link StockItem}.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    /**
     * Vyhledá stav zásoby konkrétního produktu na konkrétním skladu.
     */
    Optional<StockItem> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    /**
     * Vrátí všechny skladové položky pro daný produkt (napříč všemi sklady).
     */
    @Query("SELECT s FROM StockItem s JOIN FETCH s.warehouse WHERE s.product.id = :productId")
    List<StockItem> findByProductIdWithWarehouse(@Param("productId") Long productId);

    /**
     * Vrátí všechny položky na daném skladu.
     */
    @Query("SELECT s FROM StockItem s JOIN FETCH s.product WHERE s.warehouse.id = :warehouseId")
    List<StockItem> findByWarehouseIdWithProduct(@Param("warehouseId") Long warehouseId);

    /**
     * Spočítá celkovou fyzickou zásobu produktu napříč všemi sklady.
     */
    @Query("SELECT COALESCE(SUM(s.physicalQuantity), 0) FROM StockItem s WHERE s.product.id = :productId")
    Integer sumPhysicalQuantityByProductId(@Param("productId") Long productId);

    /**
     * Spočítá celkovou disponibilní (volnou) zásobu produktu napříč všemi sklady.
     */
    @Query("SELECT COALESCE(SUM(s.physicalQuantity - s.reservedQuantity), 0) FROM StockItem s WHERE s.product.id = :productId")
    Integer sumAvailableQuantityByProductId(@Param("productId") Long productId);
}

