package cz.uhk.fim.ppro.warehouse.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Doménová entita zákaznické objednávky e-shopu Dřevěnka s.r.o.
 *
 * <p>
 * <b>Kritický bod zadání B – Rezervační mechanismus:</b>
 * Při přijetí objednávky (přechod do stavu {@code RESERVED}) systém atomicky zvýší
 * {@code reserved_quantity} v tabulce {@code stock_items}. Tím se okamžitě sníží
 * disponibilní zásoba pro ostatní zákazníky a zamezí se prodeji neexistujícího zboží.
 * </p>
 *
 * <p>
 * <b>Životní cyklus objednávky (stavový automat):</b>
 * <pre>
 *   CREATED → RESERVED → PROCESSING → DISPATCHED
 *                ↓             ↓
 *            CANCELLED     CANCELLED
 * </pre>
 * </p>
 *
 * <p>
 * <b>CREATED:</b>  Objednávka přijata, zásoba ještě nebyla rezervována.<br>
 * <b>RESERVED:</b> Zásoba je blokována pro zákazníka (reserved_quantity++).<br>
 * <b>PROCESSING:</b> Sklad vychystává zásilku.<br>
 * <b>DISPATCHED:</b> Expedováno; physical_quantity-- a reserved_quantity-- proběhly.<br>
 * <b>CANCELLED:</b> Stornováno; rezervace uvolněna (reserved_quantity--).<br>
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_customer", columnList = "customer_id"),
    @Index(name = "idx_orders_status",   columnList = "status"),
    @Index(name = "idx_orders_number",   columnList = "order_number")
})
public class Order {

    /**
     * Výčet stavů objednávky. Stavy jsou uspořádány lineárně (stavový automat).
     */
    public enum OrderStatus {
        /** Nová objednávka, zásoba ještě nebyla rezervována. */
        CREATED,
        /** Zásoba byla atomicky rezervována (reserved_quantity++ v stock_items). */
        RESERVED,
        /** Sklad vychystává zásilku – objednávka je zamčena pro přímé úpravy. */
        PROCESSING,
        /** Expedováno; fyzická zásoba odečtena, rezervace uvolněna. */
        DISPATCHED,
        /** Stornováno; případné rezervace uvolněny zpět do disponibilní zásoby. */
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Čitelný kód objednávky pro zákazníka a sklaniška (např. ORD-2026-00001).
     * Unikátní v rámci celého systému.
     */
    @Column(name = "order_number", nullable = false, unique = true, length = 32)
    private String orderNumber;

    /**
     * Zákazník, který objednávku zadal.
     * Zákazníci se nemažou – vazba tedy nikdy nezpůsobí osiřelý záznam.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Aktuální stav objednávky (stavový automat).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OrderStatus status = OrderStatus.CREATED;

    /**
     * Celková cena objednávky v CZK.
     * Vypočítává se jako součet (quantity × unit_price_at_purchase) za všechny položky.
     */
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    /** Volitelná poznámka zákazníka (instrukce ke zpracování, dárkové balení apod.). */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Položky objednávky. Každá položka odkazuje na konkrétní produkt a sklad výdeje.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Order() {}

    // =====================================================================
    // Gettery a settery
    // =====================================================================

    public Long getId() { return id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
