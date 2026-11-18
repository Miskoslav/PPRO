package cz.uhk.fim.ppro.warehouse.service;

import cz.uhk.fim.ppro.warehouse.dto.WarehouseCreateDto;
import cz.uhk.fim.ppro.warehouse.dto.WarehouseInventoryItemDto;
import cz.uhk.fim.ppro.warehouse.dto.WarehouseSummaryDto;
import cz.uhk.fim.ppro.warehouse.model.Warehouse;

import java.util.List;

/**
 * Servisní rozhraní pro správu skladů a inventáře na jednotlivých skladech.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
public interface WarehouseService {

    /**
     * Vrátí přehled všech aktivních skladů.
     */
    List<Warehouse> getAllWarehouses();

    /**
     * Vrátí přehled skladů s agregovanými počty zásob (fyzická, rezervovaná, disponibilní).
     */
    List<WarehouseSummaryDto> getWarehousesSummary();

    /**
     * Vrátí podrobný inventář daného skladu (seznam produktů a jejich stav zásob).
     *
     * @param warehouseId ID skladu
     */
    List<WarehouseInventoryItemDto> getWarehouseInventory(Long warehouseId);

    /**
     * Vytvoří nový sklad (pokud e-shop expanduje a otevře další pobočku).
     */
    Warehouse createWarehouse(WarehouseCreateDto dto);
}
