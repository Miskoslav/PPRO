package cz.uhk.fim.ppro.warehouse.api;

import cz.uhk.fim.ppro.warehouse.model.Warehouse;
import cz.uhk.fim.ppro.warehouse.repository.WarehouseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller pro přehled skladů Dřevěnka s.r.o.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@RestController
@RequestMapping("/api/v1/warehouses")
@Tag(name = "Správa skladů", description = "Přehled skladových prostor (Hradec Králové a Třebechovice pod Orebem)")
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;

    public WarehouseController(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @GetMapping
    @Operation(summary = "Získat seznam aktivních skladů", description = "Vrátí seznam evidovaných skladů Dřevěnky.")
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseRepository.findByIsActiveTrue());
    }
}

