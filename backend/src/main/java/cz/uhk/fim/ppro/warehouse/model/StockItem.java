package cz.uhk.fim.ppro.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * JPA Entita reprezentující evidenci skladové zásoby konkrétního produktu na konkrétním skladu.
 * <p>
 * Ošetřuje klíčová obchodní pravidla zadání B:
 * <ul>
 *   <li><b>Dvoufázový model zásob:</b>
 *       {@code physicalQuantity} (fyzický stav v regálu) vs.
 *       {@code reservedQuantity} (rezervováno rozpracovanými objednávkami).</li>
 *   <li><b>Disponibilní zásoba:</b>
 *       {@code availableQuantity = physicalQuantity - reservedQuantity}
 *       (množství, které lze okamžitě nabídnout dalším zákazníkům).</li>
 *   <li><b>Zamezení záporu:</b>
 *       Fyzická ani rezervovaná zásoba nesmí být záporná a rezervace nesmí převýšit fyzický stav.</li>
 * </ul>
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Entity
@Table(name = "stock_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_stock_items_product_warehouse", columnNames = {"product_id", "warehouse_id"})
})
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    /**
     * Fyzický počet kusů nacházející se aktuálně v regálu daného skladu.
     */
    @NotNull
    @Min(value = 0, message = "Fyzická zásoba nesmí být záporná")
    @Column(name = "physical_quantity", nullable = false)
    private Integer physicalQuantity = 0;

    /**
     * Počet kusů zarezervovaný pro dosud neodeslané objednávky.
     */
    @NotNull
    @Min(value = 0, message = "Rezervovaná zásoba nesmí být záporná")
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public StockItem() {
    }

    public StockItem(Product product, Warehouse warehouse, Integer physicalQuantity, Integer reservedQuantity) {
        this.product = product;
        this.warehouse = warehouse;
        this.physicalQuantity = physicalQuantity != null ? physicalQuantity : 0;
        this.reservedQuantity = reservedQuantity != null ? reservedQuantity : 0;
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
        if (this.physicalQuantity == null) this.physicalQuantity = 0;
        if (this.reservedQuantity == null) this.reservedQuantity = 0;
    }

    /**
     * Spočítá okamžitou disponibilní (volnou) zásobu pro prodej.
     *
     * @return disponibilní zásoba = fyzická zásoba - rezervace
     */
    public int getAvailableQuantity() {
        int physical = physicalQuantity != null ? physicalQuantity : 0;
        int reserved = reservedQuantity != null ? reservedQuantity : 0;
        return Math.max(0, physical - reserved);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public Integer getPhysicalQuantity() {
        return physicalQuantity;
    }

    public void setPhysicalQuantity(Integer physicalQuantity) {
        this.physicalQuantity = physicalQuantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

