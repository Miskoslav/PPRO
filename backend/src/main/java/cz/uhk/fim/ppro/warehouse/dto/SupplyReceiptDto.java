package cz.uhk.fim.ppro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Přepravka pro naskladnění doručené zásilky od dodavatele na konkrétní sklad.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Schema(description = "Požadavek na naskladnění doručeného zboží")
public class SupplyReceiptDto {

    @Schema(description = "ID cílového skladu pro naskladnění (1 = Hradec, 2 = Třebechovice)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Cílový sklad je povinný údaj")
    private Long warehouseId;

    @Schema(description = "Počet naskladňovaných kusů", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Počet kusů k naskladnění je povinný")
    @Positive(message = "Naskladňovaný počet kusů musí být kladné číslo")
    private Integer quantity;

    public SupplyReceiptDto() {
    }

    public SupplyReceiptDto(Long warehouseId, Integer quantity) {
        this.warehouseId = warehouseId;
        this.quantity = quantity;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

