package cz.uhk.fim.ppro.warehouse.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Položka zákaznické objednávky – propojuje objednávku s konkrétním produktem.
 *
 * <p>
 * <b>Klíčové byznys pravidlo – fixace ceny:</b>
 * Pole {@code unitPriceAtPurchase} uchovává prodejní cenu produktu v okamžiku
 * vytvoření objednávky. Pokud se ceník v katalogu změní, historická cena v objednávce
 * zůstane nedotčena. Splňuje klientovu podmínku:
 * <i>"Cena v odeslané objednávce se nesmí zpětně změnit."</i>
 * </p>
 *
 * <p>
 * <b>Vazba na sklad výdeje (warehouse_id):</b>
 * Každá položka určuje, ze kterého konkrétního skladu (Hradec Králové nebo Třebechovice)
 * bude zboží fyzicky vydáno. To umožňuje split-fulfillment a přesnou auditní stopu.
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_items_order",   columnList = "order_id"),
    @Index(name = "idx_order_items_product", columnList = "product_id")
})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Objednávka, do níž tato položka patří.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Produkt, který byl objednán.
     * DELETE RESTRICT na DB úrovni zabrání smazání produktu s historií v objednávkách.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Sklad, z nějž bude zboží fyzicky vydáno.
     * Nullable – sklad se může upřesnit až při vychystávání (stav PROCESSING).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    /**
     * Objednané množství v kusech. Musí být kladné číslo.
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Zafixovaná prodejní cena v CZK v okamžiku vytvoření objednávky.
     * Tato hodnota se NIKDY NEMĚNÍ – i pokud se ceník produktu aktualizuje.
     * Byznys pravidlo Dřevěnky: prodejní cena v objednávce je závazná.
     */
    @Column(name = "unit_price_at_purchase", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceAtPurchase;

    public OrderItem() {}

    public OrderItem(Order order, Product product, Warehouse warehouse,
                     Integer quantity, BigDecimal unitPriceAtPurchase) {
        this.order = order;
        this.product = product;
        this.warehouse = warehouse;
        this.quantity = quantity;
        this.unitPriceAtPurchase = unitPriceAtPurchase;
    }

    // =====================================================================
    // Gettery a settery
    // =====================================================================

    public Long getId() { return id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPriceAtPurchase() { return unitPriceAtPurchase; }
    public void setUnitPriceAtPurchase(BigDecimal unitPriceAtPurchase) {
        this.unitPriceAtPurchase = unitPriceAtPurchase;
    }

    /**
     * Vypočte celkovou cenu za tuto položku (quantity × unitPriceAtPurchase).
     */
    public BigDecimal getLineTotal() {
        if (quantity == null || unitPriceAtPurchase == null) return BigDecimal.ZERO;
        return unitPriceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }
}
