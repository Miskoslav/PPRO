package cz.uhk.fim.ppro.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entita reprezentující položku sortimentu (produkt / hračku) e-shopu Dřevěnka s.r.o.
 * <p>
 * Tato entita tvoří základ katalogu produktů a je první implementovanou entitou
 * v rámci semestrálního projektu předmětu Pokročilé programování (PPRO, FIM UHK).
 * </p>
 * <p>
 * <b>Obchodní pravidla a invarianty (zadání B &amp; AGENTS.md):</b>
 * <ul>
 *   <li>Každý produkt má unikátní skladový kód {@code sku} pro jednoznačné určení v regálu.</li>
 *   <li>Nákupní i prodejní cena musí být nezáporná (hlídáno anotacemi i DB CHECK omezením).</li>
 *   <li>Minimální bezpečná skladová zásoba {@code minStockLevel} slouží pro vyhodnocení
 *       prioritního doobjednání (dashboard "co hoří").</li>
 *   <li>Při smazání produktu se nastavuje {@code isActive = false} (soft-delete),
 *       aby se neporušila integrita historických objednávek.</li>
 * </ul>
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Entity
@Table(name = "products")
public class Product {

    /**
     * Jedinečný primární identifikátor produktu generovaný databází.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unikátní skladový identifikátor (SKU - Stock Keeping Unit), např. "HRA-KAC-001".
     * Používá se pro označení zboží na štítcích a v interních skladových procesech.
     */
    @NotBlank(message = "SKU kód nesmí být prázdný")
    @Size(max = 64, message = "SKU kód smí mít maximálně 64 znaků")
    @Column(name = "sku", nullable = false, unique = true, length = 64)
    private String sku;

    /**
     * Obchodní název dřevěné hračky (např. "Dřevěná káča pestrobarevná").
     */
    @NotBlank(message = "Název produktu nesmí být prázdný")
    @Size(max = 255, message = "Název produktu smí mít maximálně 255 znaků")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Detailní popis hračky, použitého materiálu (např. bukové dřevo) a certifikací.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Nákupní cena za jednotku v CZK (za kolik e-shop nakupuje od truhláře/výrobce).
     * Slouží pro výpočet marže a ziskovosti po kategoriích.
     */
    @NotNull(message = "Nákupní cena je povinná")
    @DecimalMin(value = "0.00", message = "Nákupní cena nesmí být záporná")
    @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    /**
     * Prodejní cena za jednotku v CZK včetně DPH (za kolik zákazník nakupuje na e-shopu).
     * Klíčové pravidlo: Prodejní cena se při objednávce fixuje a nesmí se zpětně měnit.
     */
    @NotNull(message = "Prodejní cena je povinná")
    @DecimalMin(value = "0.00", message = "Prodejní cena nesmí být záporná")
    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    /**
     * Minimální bezpečná skladová zásoba v kusech.
     * Pokud celková dostupná zásoba klesne pod tuto hodnotu, produkt se zobrazí
     * na dashboardu v seznamu prioritních doobjednávek ("co hoří").
     */
    @NotNull(message = "Minimální zásoba je povinná")
    @Min(value = 0, message = "Minimální zásoba nesmí být záporná")
    @Column(name = "min_stock_level", nullable = false)
    private Integer minStockLevel = 0;

    /**
     * Množství kusů objednaných u truhláře / výrobce, které je aktuálně na cestě.
     * Pokud je celková zásoba pod minimem, ale {@code incomingQuantity > 0},
     * systém nehlásí kritický poplach, nýbrž informativní stav "Objednáno u dodavatele".
     */
    @NotNull(message = "Objednané množství nesmí být null")
    @Min(value = 0, message = "Objednané množství nesmí být záporné")
    @Column(name = "incoming_quantity", nullable = false)
    private Integer incomingQuantity = 0;

    /**
     * Vazba M:N – kategorie, do kterých je tato hračka zařazena.
     * Příklad ze zadání: káča patří současně do "Pro batolata" i "Dárky do 500 Kč".
     */
    @jakarta.persistence.ManyToMany(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinTable(
            name = "product_categories",
            joinColumns = @jakarta.persistence.JoinColumn(name = "product_id"),
            inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "category_id")
    )
    private java.util.Set<Category> categories = new java.util.HashSet<>();

    /**
     * Příznak, zda je produkt aktivní a nabízen k prodeji.
     * Deaktivované položky se nenabízí v novém katalogu, ale zůstávají v DB pro historii.
     */
    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Časové razítko vytvoření záznamu v systému.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Časové razítko poslední aktualizace záznamu.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Bezparametrický konstruktor vyžadovaný specifikací JPA.
     */
    public Product() {
    }

    /**
     * Konstruktor pro pohodlné vytvoření nového produktu se základními atributy.
     *
     * @param sku            skladový kód
     * @param name           název produktu
     * @param description    popis produktu
     * @param purchasePrice  nákupní cena
     * @param sellingPrice   prodejní cena
     * @param minStockLevel  minimální bezpečná zásoba
     */
    public Product(String sku, String name, String description, BigDecimal purchasePrice,
                   BigDecimal sellingPrice, Integer minStockLevel) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.minStockLevel = minStockLevel != null ? minStockLevel : 0;
        this.isActive = true;
    }

    /**
     * Automatické nastavení časových razítek před vložením do databáze.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.minStockLevel == null) {
            this.minStockLevel = 0;
        }
        if (this.incomingQuantity == null) {
            this.incomingQuantity = 0;
        }
    }

    /**
     * Automatická aktualizace časového razítka před úpravou záznamu.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================================================================
    // Gettery a Settery
    // =========================================================================

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

    public Integer getIncomingQuantity() {
        return incomingQuantity;
    }

    public void setIncomingQuantity(Integer incomingQuantity) {
        this.incomingQuantity = incomingQuantity;
    }

    public java.util.Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(java.util.Set<Category> categories) {
        this.categories = categories;
    }

    public void addCategory(Category category) {
        this.categories.add(category);
        category.getProducts().add(this);
    }

    public void removeCategory(Category category) {
        this.categories.remove(category);
        category.getProducts().remove(this);
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

    // =========================================================================
    // Pomocné metody (equals, hashCode, toString)
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        // Obchodní klíč je SKU – dvě položky se stejným kódem jsou totožný artikl
        return Objects.equals(sku, product.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", purchasePrice=" + purchasePrice +
                ", sellingPrice=" + sellingPrice +
                ", minStockLevel=" + minStockLevel +
                ", isActive=" + isActive +
                '}';
    }
}

