package cz.uhk.fim.ppro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO pro vytvoření nové zákaznické objednávky.
 *
 * <p>
 * Objednávka se vytváří v jednom volání API – obsahuje ID zákazníka
 * a seznam položek (produkt + sklad + množství). Systém sám:
 * <ol>
 *   <li>Vygeneruje číslo objednávky (ORD-YYYY-XXXXX).</li>
 *   <li>Zafixuje aktuální prodejní ceny všech položek.</li>
 *   <li>Pokud je dostupná zásoba, přejde do stavu RESERVED a atomicky
 *       navýší reserved_quantity pro každý produkt na daném skladu.</li>
 *   <li>Pokud zásoba nestačí, vrátí chybu 422 (Unprocessable Entity).</li>
 * </ol>
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Schema(description = "Požadavek na vytvoření nové zákaznické objednávky")
public class OrderCreateDto {

    @Schema(description = "ID zákazníka, který objednávku zadává", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID zákazníka je povinné")
    private Long customerId;

    @Schema(description = "Volitelná poznámka zákazníka (instrukce ke zpracování, dárkové balení apod.)")
    @Size(max = 1000, message = "Poznámka může mít maximálně 1000 znaků")
    private String note;

    @Schema(description = "Položky objednávky (minimálně jedna)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Objednávka musí obsahovat alespoň jednu položku")
    @Valid
    private List<OrderItemCreateDto> items;

    public OrderCreateDto() {}

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<OrderItemCreateDto> getItems() { return items; }
    public void setItems(List<OrderItemCreateDto> items) { this.items = items; }

    // =====================================================================
    // Vnořené DTO pro položky objednávky
    // =====================================================================

    /**
     * Položka v požadavku na vytvoření objednávky.
     */
    @Schema(description = "Jedna položka objednávky")
    public static class OrderItemCreateDto {

        @Schema(description = "ID produktu", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID produktu je povinné")
        private Long productId;

        @Schema(description = "ID skladu, ze kterého bude zboží vydáno", example = "1")
        private Long warehouseId;

        @Schema(description = "Objednávané množství v kusech", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Množství je povinné")
        @Positive(message = "Množství musí být kladné číslo")
        private Integer quantity;

        public OrderItemCreateDto() {}

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Long getWarehouseId() { return warehouseId; }
        public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
