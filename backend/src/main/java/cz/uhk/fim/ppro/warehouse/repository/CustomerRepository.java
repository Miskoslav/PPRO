package cz.uhk.fim.ppro.warehouse.repository;

import cz.uhk.fim.ppro.warehouse.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repozitář pro zákazníky e-shopu Dřevěnka s.r.o.
 *
 * <p>
 * Zákazníci se z databáze NIKDY NEMAŽOU. Tento repozitář proto záměrně
 * neposkytuje žádnou metodu pro mazání zákazníků – operace {@code delete}
 * zděděná z JpaRepository je blokována na úrovni servisní vrstvy.
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Vyhledá všechny zákazníky seřazené abecedně podle příjmení a křestního jména.
     * Používá se pro výběr zákazníka při zadávání nové objednávky.
     */
    List<Customer> findAllByOrderByLastNameAscFirstNameAsc();

    /**
     * Vyhledá zákazníky podle e-mailové adresy.
     * Pomáhá při identifikaci vracejícího se zákazníka z e-shopu.
     */
    List<Customer> findByEmail(String email);
}
