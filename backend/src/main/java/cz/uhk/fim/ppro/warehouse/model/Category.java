package cz.uhk.fim.ppro.warehouse.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * JPA Entita reprezentující kategorii dřevěných hraček (např. "Pro batolata", "Dárky do 500 Kč").
 * <p>
 * Realizuje povinné minimum PPRO pro vazbu M:N (jeden produkt může náležet do více kategorií
 * a v jedné kategorii je více produktů).
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Zobrazovaný název kategorie (např. "Dárky do 500 Kč").
     */
    @NotBlank(message = "Název kategorie je povinný")
    @Size(max = 128, message = "Název kategorie smí mít maximálně 128 znaků")
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    /**
     * Unikátní URL identifikátor / kód kategorie (např. "darky-do-500").
     */
    @NotBlank(message = "Slug kategorie je povinný")
    @Size(max = 128, message = "Slug smí mít maximálně 128 znaků")
    @Column(name = "slug", nullable = false, unique = true, length = 128)
    private String slug;

    /**
     * Doplňující popis kategorie hraček pro zákazníky i personál.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Vazba M:N – produkty zařazené do této kategorie.
     */
    @ManyToMany(mappedBy = "categories")
    @JsonIgnore
    private Set<Product> products = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Category() {
    }

    public Category(String name, String slug, String description) {
        this.name = name;
        this.slug = slug;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
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
        if (!(o instanceof Category category)) return false;
        return Objects.equals(slug, category.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slug);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                '}';
    }
}

