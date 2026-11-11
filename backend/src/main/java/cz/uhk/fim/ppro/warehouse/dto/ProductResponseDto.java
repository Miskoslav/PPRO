package cz.uhk.fim.ppro.warehouse.dto;

import cz.uhk.fim.ppro.warehouse.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Přepravka dat (Data Transfer Object) reprezentující detail produktu odesílaný klientovi.
 * <p>
 * Obohacuje surová entitní data o kalkulované ukazatele:
 * <ul>
 *   <li>{@code marginAmount} – marže v absolutní hodnotě v CZK (prodejní cena - nákupní cena).</li>
 *   <li>{@code marginPercentage} – obchodní přirážka / marže v procentech.</li>
 *   <li>{@code totalQuantity} – celková fyzická zásoba napříč všemi sklady.</li>
 *   <li>{@code availableQuantity} – volná disponibilní zásoba (fyzická - rezervace).</li>
 *   <li>{@code incomingQuantity} – zboží objednané u dodavatele, které je aktuálně na cestě.</li>
 *   <li>{@code stockStatus} – vyhodnocený stav ("OK", "LOW", "CRITICAL", "ON_ORDER").</li>
 *   <li>{@code stockStatusLabel} – lidsky srozumitelný popisek stavu.</li>
 * </ul>
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Schema(description = "Reprezentace produktu vrácená z aplikačního rozhraní")
public class ProductResponseDto {

    @Schema(description = "Jedinečné ID produktu", example = "1")
    private Long id;

    @Schema(description = "Skladový kód položky (SKU)", example = "HRA-KAC-001")
    private String sku;

    @Schema(description = "Alternativní alias pro frontend (kompatibilita se šablonami)", example = "HRA-KAC-001")
    private String skuCode;

    @Schema(description = "Název hračky", example = "Dřevěná káča pestrobarevná")
    private String name;

    @Schema(description = "Detailní popis hračky", example = "Tradiční ručně soustružená káča...")
    private String description;

    @Schema(description = "Nákupní cena v CZK", example = "45.00")
    private BigDecimal purchasePrice;

    @Schema(description = "Prodejní cena v CZK", example = "99.00")
    private BigDecimal sellingPrice;

    @Schema(description = "Kalkulovaná marže v CZK", example = "54.00")
    private BigDecimal marginAmount;

    @Schema(description = "Kalkulovaná procentuální marže", example = "54.55")
    private BigDecimal marginPercentage;

    @Schema(description = "Minimální bezpečná skladová zásoba v ks", example = "15")
    private Integer minStockLevel;

    @Schema(description = "Aktuální celková fyzická skladová zásoba v ks", example = "30")
    private Integer totalQuantity = 0;

    @Schema(description = "Disponibilní zásoba pro nový prodej v ks (fyzická - rezervace)", example = "27")
    private Integer availableQuantity = 0;

    @Schema(description = "Počet kusů objednaných u truhláře / dodavatele, které jsou na cestě", example = "5")
    private Integer incomingQuantity = 0;

    @Schema(description = "Kód stavu zásoby: OK, LOW, CRITICAL, ON_ORDER", example = "OK")
    private String stockStatus = "OK";

    @Schema(description = "Lidský popisek stavu zásoby", example = "V pořádku")
    private String stockStatusLabel = "V pořádku";

    @Schema(description = "Seznam přiřazených kategorií")
    private List<String> categories = new ArrayList<>();

    @Schema(description = "Rozpad zásoby po jednotlivých skladech")
    private Map<String, Integer> warehouseStock = new HashMap<>();

    @Schema(description = "Příznak aktivity v katalogu", example = "true")
    private Boolean isActive;

    @Schema(description = "Datum a čas zaevidování hračky")
    private LocalDateTime createdAt;

    @Schema(description = "Datum a čas poslední změny")
    private LocalDateTime updatedAt;

    public ProductResponseDto() {
    }

    public static ProductResponseDto fromEntity(Product product) {
        return fromEntity(product, 0, 0, new HashMap<>());
    }

    public static ProductResponseDto fromEntity(Product product, Integer totalQuantity) {
        return fromEntity(product, totalQuantity, totalQuantity, new HashMap<>());
    }

    public static ProductResponseDto fromEntity(Product product, Integer physicalQty, Integer availableQty, Map<String, Integer> warehouseStock) {
        if (product == null) {
            return null;
        }

        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setSkuCode(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPurchasePrice(product.getPurchasePrice());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setMinStockLevel(product.getMinStockLevel());
        dto.setTotalQuantity(physicalQty != null ? physicalQty : 0);
        dto.setAvailableQuantity(availableQty != null ? availableQty : (physicalQty != null ? physicalQty : 0));
        dto.setIncomingQuantity(product.getIncomingQuantity() != null ? product.getIncomingQuantity() : 0);
        dto.setIsActive(product.getIsActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        if (warehouseStock != null) {
            dto.setWarehouseStock(new HashMap<>(warehouseStock));
        }

        if (product.getCategories() != null) {
            dto.setCategories(product.getCategories().stream().map(c -> c.getName()).toList());
        }

        // Výpočet marže
        if (product.getSellingPrice() != null && product.getPurchasePrice() != null) {
            BigDecimal margin = product.getSellingPrice().subtract(product.getPurchasePrice());
            dto.setMarginAmount(margin);

            if (product.getSellingPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = margin
                        .divide(product.getSellingPrice(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                dto.setMarginPercentage(percentage);
            } else {
                dto.setMarginPercentage(BigDecimal.ZERO);
            }
        }

        // Vyhodnocení stavu zásoby s ohledem na zboží na cestě (uživatelský požadavek)
        int min = product.getMinStockLevel() != null ? product.getMinStockLevel() : 0;
        int avail = dto.getAvailableQuantity();
        int incoming = dto.getIncomingQuantity();

        if (avail < min) {
            if (incoming > 0) {
                dto.setStockStatus("ON_ORDER");
                dto.setStockStatusLabel("Na cestě (" + incoming + " ks)");
            } else {
                dto.setStockStatus("CRITICAL");
                dto.setStockStatusLabel("Kritický stav");
            }
        } else if (avail < min * 1.5) {
            dto.setStockStatus("LOW");
            dto.setStockStatusLabel("Nízký stav");
        } else {
            dto.setStockStatus("OK");
            dto.setStockStatusLabel("V pořádku");
        }

        return dto;
    }

    // Gettery a Settery

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
        this.skuCode = sku;
    }

    public String getSkuCode() {
        return skuCode != null ? skuCode : sku;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
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

    public BigDecimal getMarginAmount() {
        return marginAmount;
    }

    public void setMarginAmount(BigDecimal marginAmount) {
        this.marginAmount = marginAmount;
    }

    public BigDecimal getMarginPercentage() {
        return marginPercentage;
    }

    public void setMarginPercentage(BigDecimal marginPercentage) {
        this.marginPercentage = marginPercentage;
    }

    public Integer getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getIncomingQuantity() {
        return incomingQuantity;
    }

    public void setIncomingQuantity(Integer incomingQuantity) {
        this.incomingQuantity = incomingQuantity;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public String getStockStatusLabel() {
        return stockStatusLabel;
    }

    public void setStockStatusLabel(String stockStatusLabel) {
        this.stockStatusLabel = stockStatusLabel;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public Map<String, Integer> getWarehouseStock() {
        return warehouseStock;
    }

    public void setWarehouseStock(Map<String, Integer> warehouseStock) {
        this.warehouseStock = warehouseStock;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
