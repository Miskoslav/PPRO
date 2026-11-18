package cz.uhk.fim.ppro.warehouse.api;

import cz.uhk.fim.ppro.warehouse.model.Customer;
import cz.uhk.fim.ppro.warehouse.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST Controller pro zákazníky e-shopu Dřevěnka s.r.o.
 *
 * <p>Poskytuje seznam zákazníků pro výběr při zadávání nové objednávky ve webovém rozhraní.</p>
 *
 * <p>Základní URL: {@code /api/v1/customers}</p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Zákazníci", description = "Evidence zákazníků e-shopu")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    @Operation(summary = "Seznam zákazníků", description = "Vrátí seznam všech zákazníků seřazených abecedně pro použití v selectu při zadávání objednávky.")
    public ResponseEntity<List<Map<String, Object>>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAllByOrderByLastNameAscFirstNameAsc();
        List<Map<String, Object>> result = customers.stream()
                .map(c -> Map.<String, Object>of(
                        "id", c.getId(),
                        "fullName", c.getFullName(),
                        "email", c.getEmail(),
                        "phone", c.getPhone() != null ? c.getPhone() : ""
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
}
