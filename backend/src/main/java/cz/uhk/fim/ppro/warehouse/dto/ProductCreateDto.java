package cz.uhk.fim.ppro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Přepravka dat (Data Transfer Object) pro vytvoření nového produktu / hračky.
 * <p>
 * Obsahuje validační anotace Jakarta Bean Validation pro ověření korektnosti vstupů
 * dříve, než jsou předány do doménové vrstvy.
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Schema(description = "Požadavek na vytvoření nového produktu do katalogu")
public class ProductCreateDto {

    @Schema(description = "Unikátní skladový kód (SKU)", example = "HRA-KAC-002", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "SKU kód je povinný údaj")
    @Size(max = 64, message = "SKU kód smí mít maximálně 64 znaků")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "SKU kód smí obsahovat pouze velká písmena, číslice, pomlčky a podtržítka")
    private String sku;

    @Schema(description = "Název dřevěné hračky", example = "Dřevěná káča přírodní", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Název hračky je povinný údaj")
    @Size(max = 255, message = "Název hračky smí mít maximálně 255 znaků")
    private String name;

    @Schema(description = "Podrobný popis hračky a materiálu", example = "Přírodní neleštěná buková káča bez nátěru.")
    private String description;

    @Schema(description = "Nákupní cena v CZK bez DPH", example = "35.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Nákupní cena je povinná")
    @DecimalMin(value = "0.00", message = "Nákupní cena nesmí být záporná")
    private BigDecimal purchasePrice;

    @Schema(description = "Prodejní cena v CZK včetně DPH", example = "79.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Prodejní cena je povinná")
    @DecimalMin(value = "0.00", message = "Prodejní cena nesmí být záporná")
    private BigDecimal sellingPrice;

    @Schema(description = "Minimální bezpečná skladová zásoba v kusech", example = "10", defaultValue = "0")
    @NotNull(message = "Minimální bezpečná zásoba je povinná")
    @Min(value = 0, message = "Minimální zásoba nesmí být záporná")
    private Integer minStockLevel = 0;

    @Schema(description = "Příznak okamžité aktivace v katalogu", example = "true", defaultValue = "true")
    private Boolean isActive = true;

    public ProductCreateDto() {
    }

    public ProductCreateDto(String sku, String name, String description, BigDecimal purchasePrice,
                            BigDecimal sellingPrice, Integer minStockLevel, Boolean isActive) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.minStockLevel = minStockLevel;
        this.isActive = isActive;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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

