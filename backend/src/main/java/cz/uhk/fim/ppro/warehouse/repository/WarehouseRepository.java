package cz.uhk.fim.ppro.warehouse.repository;

import cz.uhk.fim.ppro.warehouse.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repozitář pro entitu skladu {@link Warehouse}.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    /**
     * Vyhledá sklad podle unikátního kódu (např. "HRADEC", "TREBECHOVICE").
     */
    Optional<Warehouse> findByCode(String code);

    /**
     * Vrátí seznam všech aktivních skladů.
     */
    List<Warehouse> findByIsActiveTrue();
}

