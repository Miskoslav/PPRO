package cz.uhk.fim.ppro.warehouse.api;

import cz.uhk.fim.ppro.warehouse.dto.OrderCreateDto;
import cz.uhk.fim.ppro.warehouse.dto.OrderResponseDto;
import cz.uhk.fim.ppro.warehouse.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST Controller pro zákaznické objednávky e-shopu Dřevěnka s.r.o.
 *
 * <p>
 * Jde o klíčový endpoint systému realizující kritický bod zadání B:
 * rezervační mechanismus zabraňující prodeji neexistujícího zboží.
 * Každé POST /api/v1/orders atomicky rezervuje zásobu.
 * </p>
 *
 * <p>Základní URL: {@code /api/v1/orders}</p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Zákaznické objednávky", description = "Správa objednávek, rezervace a expedice zboží")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // =====================================================================
    // GET /api/v1/orders – seznam všech objednávek
    // =====================================================================

    @GetMapping
    @Operation(summary = "Seznam objednávek", description = "Vrátí přehled všech zákaznických objednávek seřazených od nejnovější.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Přehled objednávek vrácen úspěšně")
    })
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // =====================================================================
    // GET /api/v1/orders/{id} – detail objednávky
    // =====================================================================

    @GetMapping("/{id}")
    @Operation(summary = "Detail objednávky", description = "Vrátí kompletní detail objednávky včetně položek a zákazníka.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Objednávka nalezena"),
        @ApiResponse(responseCode = "404", description = "Objednávka neexistuje")
    })
    public ResponseEntity<OrderResponseDto> getOrderById(
            @Parameter(description = "ID objednávky") @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // =====================================================================
    // POST /api/v1/orders – vytvoření nové objednávky s rezervací zásoby
    // =====================================================================

    @PostMapping
    @Operation(
        summary = "Vytvořit objednávku",
        description = """
            Vytvoří novou zákaznickou objednávku a atomicky rezervuje dostupnou zásobu.
            Pokud dostupná zásoba nestačí, vrátí chybu 422.
            Zafixuje aktuální prodejní ceny všech položek.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Objednávka úspěšně vytvořena a zásoba rezervována"),
        @ApiResponse(responseCode = "400", description = "Chybná struktura požadavku"),
        @ApiResponse(responseCode = "404", description = "Zákazník nebo produkt neexistuje"),
        @ApiResponse(responseCode = "422", description = "Nedostatečná zásoba – objednávku nelze přijmout")
    })
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderCreateDto dto) {
        OrderResponseDto created = orderService.createOrder(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // =====================================================================
    // POST /api/v1/orders/{id}/cancel – stornování objednávky
    // =====================================================================

    @PostMapping("/{id}/cancel")
    @Operation(
        summary = "Stornovat objednávku",
        description = "Stornuje objednávku ve stavu CREATED nebo RESERVED a uvolní rezervované zásoby."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Objednávka stornována, zásoby uvolněny"),
        @ApiResponse(responseCode = "404", description = "Objednávka neexistuje"),
        @ApiResponse(responseCode = "422", description = "Objednávku v aktuálním stavu nelze stornovat")
    })
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @Parameter(description = "ID objednávky") @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
