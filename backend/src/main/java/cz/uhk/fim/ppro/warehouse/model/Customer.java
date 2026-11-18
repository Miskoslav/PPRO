package cz.uhk.fim.ppro.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Doménová entita zákazníka e-shopu Dřevěnka s.r.o.
 *
 * <p>
 * <b>Klíčové byznys pravidlo (klientova podmínka č. 4):</b>
 * Zákazníci se z databáze NIKDY nemažou. Jsou nezbytní pro historii objednávek,
 * reklamace a dotazy na historická data. I po ukončení zákazníkovy aktivity
 * zůstávají jeho data v systému dostupná.
 * </p>
 *
 * <p>
 * Zákazník je fyzická nebo právnická osoba, která nakupuje produkty z e-shopu
 * Dřevěnky. Systém ukládá kontaktní a adresní údaje pro účely expedice a komunikace.
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customers_email", columnList = "email"),
    @Index(name = "idx_customers_name",  columnList = "last_name, first_name")
})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Křestní jméno zákazníka. Povinný údaj pro adresaci zásilky a komunikaci.
     */
    @Column(name = "first_name", nullable = false, length = 128)
    private String firstName;

    /**
     * Příjmení zákazníka. Povinný údaj; spolu s křestním jménem tvoří identifikaci zákazníka.
     */
    @Column(name = "last_name", nullable = false, length = 128)
    private String lastName;

    /**
     * E-mailová adresa zákazníka. Primární komunikační kanál pro potvrzení objednávek.
     * Nemusí být unikátní – jeden zákazník může nakupovat z více e-mailových adres.
     */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /** Telefonní číslo pro kontaktování zákazníka při expedici nebo problémech. */
    @Column(name = "phone", length = 32)
    private String phone;

    /** Ulice a číslo popisné doručovací adresy. */
    @Column(name = "street", length = 255)
    private String street;

    /** Město doručovací adresy. */
    @Column(name = "city", length = 128)
    private String city;

    /** Poštovní směrovací číslo doručovací adresy. */
    @Column(name = "zip", length = 16)
    private String zip;

    /**
     * Datum a čas vytvoření zákazníka v systému. Nastavuje se jednou při prvním nákupu.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Seznam objednávek tohoto zákazníka.
     * Vazba 1:N – zákazník může mít více objednávek.
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Customer() {}

    public Customer(String firstName, String lastName, String email, String phone,
                    String street, String city, String zip) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.street = street;
        this.city = city;
        this.zip = zip;
    }

    // =====================================================================
    // Gettery a settery
    // =====================================================================

    public Long getId() { return id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }

    /** Pomocná metoda pro zobrazení celého jména zákazníka. */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
