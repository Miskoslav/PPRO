package cz.uhk.fim.ppro.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entita reprezentující fyzický sklad nebo skladovací prostor e-shopu Dřevěnka s.r.o.
 * <p>
 * Klient eviduje zásoby ve dvou lokalitách:
 * <ul>
 *   <li><b>HRADEC</b> – Hlavní sklad a expediční centrum v Hradci Králové.</li>
 *   <li><b>TREBECHOVICE</b> – Sklad sezónních přebytků v Třebechovicích pod Orebem (garáž).</li>
 * </ul>
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Entity
@Table(name = "warehouses")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unikátní kód skladu (např. "HRADEC", "TREBECHOVICE").
     */
    @NotBlank(message = "Kód skladu je povinný")
    @Size(max = 32, message = "Kód skladu smí mít maximálně 32 znaků")
    @Column(name = "code", nullable = false, unique = true, length = 32)
    private String code;

    /**
     * Lidsky srozumitelný název skladu (např. "Hlavní sklad Hradec Králové").
     */
    @NotBlank(message = "Název skladu je povinný")
    @Size(max = 255, message = "Název skladu smí mít maximálně 255 znaků")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Fyzická adresa skladu.
     */
    @Size(max = 255)
    @Column(name = "address", length = 255)
    private String address;

    /**
     * Příznak, zda je sklad aktivní v provozu.
     */
    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Časové razítko zaevidování skladu v systému.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Warehouse() {
    }

    public Warehouse(String code, String name, String address) {
        this.code = code;
        this.name = name;
        this.address = address;
        this.isActive = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Warehouse warehouse)) return false;
        return Objects.equals(code, warehouse.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Warehouse{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}

