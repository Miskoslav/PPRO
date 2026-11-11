package cz.uhk.fim.ppro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Přepravka pro zadání doobjednávky zboží u truhláře / dodavatele.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Schema(description = "Požadavek na doobjednání zboží u dodavatele (zboží na cestě)")
public class SupplyOrderDto {

    @Schema(description = "Počet objednávaných kusů", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Počet kusů je povinný")
    @Positive(message = "Objednávaný počet kusů musí být kladné číslo")
    private Integer quantity;

    public SupplyOrderDto() {
    }

    public SupplyOrderDto(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

