package cz.uhk.fim.ppro.warehouse.dto;

import cz.uhk.fim.ppro.warehouse.model.Order;
import cz.uhk.fim.ppro.warehouse.model.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pro výstup zákaznické objednávky – přenáší data z doménové vrstvy do API.
 *
 * <p>
 * Obsahuje kompletní přehled objednávky pro zobrazení ve webovém rozhraní,
 * včetně zákaznických údajů, stavu, celkové ceny a jednotlivých položek
 * se zafixovanými cenami.
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
public class OrderResponseDto {

    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;  // Celé jméno pro zobrazení v tabulce
    private String customerEmail;
    private String status;        // Textový kód stavu (CREATED, RESERVED, ...)
    private String statusLabel;   // Lokalizovaný český popis stavu
    private BigDecimal totalPrice;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDto> items;

    public OrderResponseDto() {}

    /**
     * Statická tovární metoda pro převod JPA entity na DTO.
     * Odděluje prezentační logiku od doménové vrstvy.
     */
    public static OrderResponseDto fromEntity(Order order, List<OrderItem> items) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.id = order.getId();
        dto.orderNumber = order.getOrderNumber();
        dto.status = order.getStatus().name();
        dto.statusLabel = resolveStatusLabel(order.getStatus());
        dto.totalPrice = order.getTotalPrice();
        dto.note = order.getNote();
        dto.createdAt = order.getCreatedAt();
        dto.updatedAt = order.getUpdatedAt();

        if (order.getCustomer() != null) {
            dto.customerId = order.getCustomer().getId();
            dto.customerName = order.getCustomer().getFullName();
            dto.customerEmail = order.getCustomer().getEmail();
        }

        if (items != null) {
            dto.items = items.stream().map(OrderItemDto::fromEntity).toList();
        }

        return dto;
    }

    /**
     * Přeloží stav objednávky do českého popisu pro uživatelské rozhraní.
     */
    private static String resolveStatusLabel(Order.OrderStatus status) {
        return switch (status) {
            case CREATED    -> "Nová";
            case RESERVED   -> "Zásoba rezervována";
            case PROCESSING -> "Ve vychystávání";
            case DISPATCHED -> "Expedováno";
            case CANCELLED  -> "Stornováno";
        };
    }

    // =====================================================================
    // Gettery
    // =====================================================================

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getStatus() { return status; }
    public String getStatusLabel() { return statusLabel; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<OrderItemDto> getItems() { return items; }

    // =====================================================================
    // Vnořené DTO pro položky objednávky
    // =====================================================================

    /**
     * Položka objednávky v odpovědi API – obsahuje zafixovanou cenu.
     */
    public static class OrderItemDto {
        private Long id;
        private Long productId;
        private String productName;
        private String productSku;
        private Long warehouseId;
        private String warehouseName;
        private Integer quantity;
        private BigDecimal unitPriceAtPurchase;
        private BigDecimal lineTotal;

        public static OrderItemDto fromEntity(OrderItem item) {
            OrderItemDto dto = new OrderItemDto();
            dto.id = item.getId();
            dto.quantity = item.getQuantity();
            dto.unitPriceAtPurchase = item.getUnitPriceAtPurchase();
            dto.lineTotal = item.getLineTotal();

            if (item.getProduct() != null) {
                dto.productId = item.getProduct().getId();
                dto.productName = item.getProduct().getName();
                dto.productSku = item.getProduct().getSku();
            }
            if (item.getWarehouse() != null) {
                dto.warehouseId = item.getWarehouse().getId();
                dto.warehouseName = item.getWarehouse().getName();
            }
            return dto;
        }

        public Long getId() { return id; }
        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getProductSku() { return productSku; }
        public Long getWarehouseId() { return warehouseId; }
        public String getWarehouseName() { return warehouseName; }
        public Integer getQuantity() { return quantity; }
        public BigDecimal getUnitPriceAtPurchase() { return unitPriceAtPurchase; }
        public BigDecimal getLineTotal() { return lineTotal; }
    }
}
