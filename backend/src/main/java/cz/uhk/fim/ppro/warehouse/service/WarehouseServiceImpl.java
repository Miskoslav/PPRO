package cz.uhk.fim.ppro.warehouse.service;

import cz.uhk.fim.ppro.warehouse.dto.WarehouseCreateDto;
import cz.uhk.fim.ppro.warehouse.dto.WarehouseInventoryItemDto;
import cz.uhk.fim.ppro.warehouse.dto.WarehouseSummaryDto;
import cz.uhk.fim.ppro.warehouse.exception.BusinessValidationException;
import cz.uhk.fim.ppro.warehouse.exception.ResourceNotFoundException;
import cz.uhk.fim.ppro.warehouse.model.StockItem;
import cz.uhk.fim.ppro.warehouse.model.Warehouse;
import cz.uhk.fim.ppro.warehouse.repository.StockItemRepository;
import cz.uhk.fim.ppro.warehouse.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementace servisní vrstvy pro sklady a inventář.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Service
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseServiceImpl.class);

    private final WarehouseRepository warehouseRepository;
    private final StockItemRepository stockItemRepository;

    public WarehouseServiceImpl(WarehouseRepository warehouseRepository,
                                StockItemRepository stockItemRepository) {
        this.warehouseRepository = warehouseRepository;
        this.stockItemRepository = stockItemRepository;
    }

    @Override
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findByIsActiveTrue();
    }

    @Override
    public List<WarehouseSummaryDto> getWarehousesSummary() {
        List<Warehouse> warehouses = warehouseRepository.findByIsActiveTrue();
        List<WarehouseSummaryDto> result = new ArrayList<>();

        for (Warehouse wh : warehouses) {
            List<StockItem> items = stockItemRepository.findByWarehouseIdWithProduct(wh.getId());
            int totalPhys = items.stream().mapToInt(si -> si.getPhysicalQuantity() != null ? si.getPhysicalQuantity() : 0).sum();
            int totalRes = items.stream().mapToInt(si -> si.getReservedQuantity() != null ? si.getReservedQuantity() : 0).sum();
            result.add(new WarehouseSummaryDto(wh, totalPhys, totalRes));
        }

        return result;
    }

    @Override
    public List<WarehouseInventoryItemDto> getWarehouseInventory(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Sklad", "id", warehouseId));

        List<StockItem> items = stockItemRepository.findByWarehouseIdWithProduct(warehouse.getId());

        return items.stream()
                .filter(si -> si.getProduct() != null && Boolean.TRUE.equals(si.getProduct().getIsActive()))
                .map(si -> new WarehouseInventoryItemDto(
                        si.getProduct().getId(),
                        si.getProduct().getSku(),
                        si.getProduct().getName(),
                        si.getPhysicalQuantity() != null ? si.getPhysicalQuantity() : 0,
                        si.getReservedQuantity() != null ? si.getReservedQuantity() : 0,
                        si.getProduct().getMinStockLevel() != null ? si.getProduct().getMinStockLevel() : 0,
                        si.getProduct().getSellingPrice()
                ))
                .toList();
    }

    @Override
    @Transactional
    public Warehouse createWarehouse(WarehouseCreateDto dto) {
        log.info("Vytváření nového skladu: kód='{}', název='{}'", dto.getCode(), dto.getName());

        if (warehouseRepository.findByCode(dto.getCode()).isPresent()) {
            throw new BusinessValidationException(String.format("Sklad s kódem '%s' již v systému existuje.", dto.getCode()));
        }

        Warehouse warehouse = new Warehouse(dto.getCode(), dto.getName(), dto.getAddress());
        return warehouseRepository.save(warehouse);
    }
}
