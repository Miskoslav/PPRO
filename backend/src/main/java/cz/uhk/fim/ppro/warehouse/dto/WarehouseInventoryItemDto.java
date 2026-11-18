package cz.uhk.fim.ppro.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Položka inventáře konkrétního skladu.
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Schema(description = "Položka inventáře skladu (konkrétní produkt a jeho zásoby na daném skladu)")
public class WarehouseInventoryItemDto {

    private Long productId;
    private String productSku;
    private String productName;
    private int physicalQuantity;
    private int reservedQuantity;
    private int availableQuantity;
    private int minStockLevel;
    private BigDecimal sellingPrice;

    public WarehouseInventoryItemDto() {}

    public WarehouseInventoryItemDto(Long productId, String productSku, String productName,
                                     int physicalQuantity, int reservedQuantity,
                                     int minStockLevel, BigDecimal sellingPrice) {
        this.productId = productId;
        this.productSku = productSku;
        this.productName = productName;
        this.physicalQuantity = physicalQuantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = Math.max(0, physicalQuantity - reservedQuantity);
        this.minStockLevel = minStockLevel;
        this.sellingPrice = sellingPrice;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getPhysicalQuantity() { return physicalQuantity; }
    public void setPhysicalQuantity(int physicalQuantity) { this.physicalQuantity = physicalQuantity; }

    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public int getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel = minStockLevel; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
}
