package cz.uhk.fim.ppro.warehouse.api;

import cz.uhk.fim.ppro.warehouse.dto.ProductCreateDto;
import cz.uhk.fim.ppro.warehouse.dto.ProductResponseDto;
import cz.uhk.fim.ppro.warehouse.dto.ProductUpdateDto;
import cz.uhk.fim.ppro.warehouse.exception.ErrorResponse;
import cz.uhk.fim.ppro.warehouse.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST Controller pro správu katalogu dřevěných hraček e-shopu Dřevěnka s.r.o.
 * <p>
 * Představuje prezentační vrstvu třívrstvé architektury PPRO.
 * Poskytuje standardní CRUD operace, filtrování a speciální pohled na položky
 * s nízkou skladovou zásobou pro dashboard.
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Katalog produktů", description = "Operace pro správu dřevěných hraček e-shopu Dřevěnka s.r.o.")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Získat seznam všech produktů", description = "Vrátí seznam produktů. Standardně vrací pouze aktivní nabízené zboží.")
    @ApiResponse(responseCode = "200", description = "Seznam produktů úspěšně načten",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponseDto.class))))
    public ResponseEntity<List<ProductResponseDto>> getAllProducts(
            @Parameter(description = "Zahrnout i vyřazené/neaktivní produkty")
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        List<ProductResponseDto> products = productService.getAllProducts(includeInactive);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Získat detail produktu podle ID", description = "Vrátí detail konkrétní dřevěné hračky včetně marže.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produkt nalezen",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Produkt se zadaným ID neexistuje",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductResponseDto> getProductById(
            @Parameter(description = "Identifikátor produktu", required = true, example = "1")
            @PathVariable Long id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Získat produkt podle SKU kódu", description = "Vyhledá položku podle skladového kódu ze štítku.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produkt nalezen"),
            @ApiResponse(responseCode = "404", description = "Produkt se zadaným SKU neexistuje")
    })
    public ResponseEntity<ProductResponseDto> getProductBySku(
            @Parameter(description = "Skladový kód SKU", required = true, example = "HRA-KAC-001")
            @PathVariable String sku) {
        ProductResponseDto product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/alerts/low-stock")
    @Operation(summary = "Položky s nízkou zásobou", description = "Vrátí seznam produktů, u kterých je zásoba pod minimálním bezpečným limitem (dashboard 'co hoří').")
    @ApiResponse(responseCode = "200", description = "Seznam ohrožených položek")
    public ResponseEntity<List<ProductResponseDto>> getLowStockAlerts() {
        List<ProductResponseDto> alerts = productService.getLowStockAlerts();
        return ResponseEntity.ok(alerts);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Vytvořit nový produkt", description = "Zaregistruje novou dřevěnou hračku do katalogu s validací unikátnosti SKU a nezáporné marže.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produkt úspěšně vytvořen",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Chyba validace vstupních dat",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Zadané SKU již existuje",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Porušení obchodního pravidla (např. záporná marže)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductCreateDto dto) {
        ProductResponseDto created = productService.createProduct(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizovat existující produkt", description = "Upraví prodejní cenu, nákupní cenu, popis nebo bezpečnou zásobu.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produkt úspěšně aktualizován",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Chyba validace vstupních dat"),
            @ApiResponse(responseCode = "404", description = "Produkt neexistuje"),
            @ApiResponse(responseCode = "422", description = "Porušení obchodního pravidla")
    })
    public ResponseEntity<ProductResponseDto> updateProduct(
            @Parameter(description = "Identifikátor produktu", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDto dto) {
        ProductResponseDto updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deaktivovat produkt (soft-delete)", description = "Vyřadí hračku z nabídky katalogu. Fyzický záznam zůstává v databázi pro historii objednávek.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produkt byl úspěšně deaktivován"),
            @ApiResponse(responseCode = "404", description = "Produkt neexistuje")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Identifikátor produktu", required = true, example = "1")
            @PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/order-supply")
    @Operation(summary = "Doobjednat zboží u dodavatele / truhláře", description = "Zadá objednávku nového zboží na cestě, čímž zklidní stav nízké zásoby na 'Na cestě'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Objednávka u dodavatele byla úspěšně zaevidována"),
            @ApiResponse(responseCode = "400", description = "Neplatný počet kusů"),
            @ApiResponse(responseCode = "404", description = "Produkt neexistuje")
    })
    public ResponseEntity<ProductResponseDto> orderSupply(
            @Parameter(description = "Identifikátor produktu", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody cz.uhk.fim.ppro.warehouse.dto.SupplyOrderDto dto) {
        ProductResponseDto updated = productService.orderSupply(id, dto.getQuantity());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/receive-supply")
    @Operation(summary = "Naskladnit doručené zboží od dodavatele", description = "Přijme doručené kusy na vybraný sklad a odečte je ze stavu 'na cestě'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zboží bylo úspěšně naskladněno"),
            @ApiResponse(responseCode = "400", description = "Neplatné parametry příjmu"),
            @ApiResponse(responseCode = "404", description = "Produkt nebo cílový sklad neexistuje")
    })
    public ResponseEntity<ProductResponseDto> receiveSupply(
            @Parameter(description = "Identifikátor produktu", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody cz.uhk.fim.ppro.warehouse.dto.SupplyReceiptDto dto) {
        ProductResponseDto updated = productService.receiveSupply(id, dto.getWarehouseId(), dto.getQuantity());
        return ResponseEntity.ok(updated);
    }
}

