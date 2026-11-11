package cz.uhk.fim.ppro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Přepravka dat (Data Transfer Object) pro úpravu stávajícího produktu.
 * <p>
 * Poznámka: SKU kód se v této DTO nenachází záměrně, protože skladový kód položky
 * je neměnným identifikátorem artiklu a nesmí být svévolně editován (konzistence skladových štítků).
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Schema(description = "Požadavek na aktualizaci parametrů produktu")
public class ProductUpdateDto {

    @Schema(description = "Nový název dřevěné hračky", example = "Dřevěná káča pestrobarevná – limitovaná edice", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Název hračky je povinný údaj")
    @Size(max = 255, message = "Název hračky smí mít maximálně 255 znaků")
    private String name;

    @Schema(description = "Aktualizovaný popis hračky", example = "Ručně malovaná limitovaná edice.")
    private String description;

    @Schema(description = "Nákupní cena v CZK bez DPH", example = "48.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Nákupní cena je povinná")
    @DecimalMin(value = "0.00", message = "Nákupní cena nesmí být záporná")
    private BigDecimal purchasePrice;

    @Schema(description = "Prodejní cena v CZK včetně DPH", example = "109.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Prodejní cena je povinná")
    @DecimalMin(value = "0.00", message = "Prodejní cena nesmí být záporná")
    private BigDecimal sellingPrice;

    @Schema(description = "Minimální bezpečná skladová zásoba v kusech", example = "20", defaultValue = "0")
    @NotNull(message = "Minimální bezpečná zásoba je povinná")
    @Min(value = 0, message = "Minimální zásoba nesmí být záporná")
    private Integer minStockLevel;

    @Schema(description = "Stav aktivity produktu v katalogu", example = "true")
    @NotNull(message = "Příznak aktivity je povinný")
    private Boolean isActive;

    public ProductUpdateDto() {
    }

    public ProductUpdateDto(String name, String description, BigDecimal purchasePrice,
                            BigDecimal sellingPrice, Integer minStockLevel, Boolean isActive) {
        this.name = name;
        this.description = description;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.minStockLevel = minStockLevel;
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}

