package cz.uhk.fim.ppro.warehouse.api;

import cz.uhk.fim.ppro.warehouse.dto.WarehouseCreateDto;
import cz.uhk.fim.ppro.warehouse.dto.WarehouseInventoryItemDto;
import cz.uhk.fim.ppro.warehouse.dto.WarehouseSummaryDto;
import cz.uhk.fim.ppro.warehouse.model.Warehouse;
import cz.uhk.fim.ppro.warehouse.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST Controller pro správu skladů a inventáře Dřevěnka s.r.o.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@RestController
@RequestMapping("/api/v1/warehouses")
@Tag(name = "Správa skladů", description = "Přehled skladových prostor a inventáře (Hradec Králové a Třebechovice pod Orebem)")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    @Operation(summary = "Seznam aktivních skladů", description = "Vrátí základní seznam evidovaných skladů Dřevěnky.")
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/summary")
    @Operation(summary = "Souhrn skladů se stavy zásob", description = "Vrátí přehled skladů s celkovými počty fyzických, rezervovaných a volných zásob.")
    public ResponseEntity<List<WarehouseSummaryDto>> getWarehousesSummary() {
        return ResponseEntity.ok(warehouseService.getWarehousesSummary());
    }

    @GetMapping("/{id}/inventory")
    @Operation(summary = "Inventář skladu", description = "Vrátí detailní rozpis všech produktů naskladněných na daném skladu.")
    public ResponseEntity<List<WarehouseInventoryItemDto>> getWarehouseInventory(
            @Parameter(description = "ID skladu") @PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseInventory(id));
    }

    @PostMapping
    @Operation(summary = "Vytvořit nový sklad", description = "Umožňuje přidat nový sklad pro případ otevření další pobočky.")
    public ResponseEntity<Warehouse> createWarehouse(@Valid @RequestBody WarehouseCreateDto dto) {
        Warehouse created = warehouseService.createWarehouse(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
