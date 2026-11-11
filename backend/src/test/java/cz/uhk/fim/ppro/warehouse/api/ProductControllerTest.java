package cz.uhk.fim.ppro.warehouse.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.uhk.fim.ppro.warehouse.dto.ProductCreateDto;
import cz.uhk.fim.ppro.warehouse.dto.ProductResponseDto;
import cz.uhk.fim.ppro.warehouse.dto.ProductUpdateDto;
import cz.uhk.fim.ppro.warehouse.exception.BusinessValidationException;
import cz.uhk.fim.ppro.warehouse.exception.DuplicateSkuException;
import cz.uhk.fim.ppro.warehouse.exception.ResourceNotFoundException;
import cz.uhk.fim.ppro.warehouse.model.Product;
import cz.uhk.fim.ppro.warehouse.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integrační testy prezentační vrstvy (REST Controller) {@link ProductController}.
 * <p>
 * Testuje mapování HTTP endpointů, validaci požadavků, HTTP stavové kódy (200, 201, 400, 404, 409, 422)
 * a formát chybových odpovědí.
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponseDto createSampleDto() {
        Product p = new Product(
                "HRA-KAC-001",
                "Dřevěná káča pestrobarevná",
                "Bukové dřevo",
                new BigDecimal("45.00"),
                new BigDecimal("99.00"),
                15
        );
        p.setId(1L);
        return ProductResponseDto.fromEntity(p, 20);
    }

    @Test
    @DisplayName("GET /api/v1/products - vrátí seznam produktů se statusem 200 OK")
    void getAllProducts_shouldReturnList() throws Exception {
        when(productService.getAllProducts(false)).thenReturn(List.of(createSampleDto()));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sku", is("HRA-KAC-001")))
                .andExpect(jsonPath("$[0].name", is("Dřevěná káča pestrobarevná")));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - existující produkt vrátí 200 OK")
    void getProductById_whenExists_shouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(createSampleDto());

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.sku", is("HRA-KAC-001")));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - neexistující produkt vrátí 404 NOT FOUND")
    void getProductById_whenNotFound_shouldReturn404() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("Produkt", "id", 999L));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Zdroj nenalezen")));
    }

    @Test
    @DisplayName("POST /api/v1/products - validní požadavek vytvoří produkt a vrátí 201 CREATED s hlavičkou Location")
    void createProduct_withValidInput_shouldReturn201() throws Exception {
        ProductCreateDto createDto = new ProductCreateDto(
                "HRA-VLA-001",
                "Dřevěný vláček",
                "Masivní dřevo",
                new BigDecimal("180.00"),
                new BigDecimal("389.00"),
                10,
                true
        );

        Product p = new Product(
                createDto.getSku(),
                createDto.getName(),
                createDto.getDescription(),
                createDto.getPurchasePrice(),
                createDto.getSellingPrice(),
                createDto.getMinStockLevel()
        );
        p.setId(2L);
        ProductResponseDto createdDto = ProductResponseDto.fromEntity(p);

        when(productService.createProduct(any(ProductCreateDto.class))).thenReturn(createdDto);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.sku", is("HRA-VLA-001")));
    }

    @Test
    @DisplayName("POST /api/v1/products - nevalidní vstup (prázdný název a záporná cena) vrátí 400 BAD REQUEST")
    void createProduct_withInvalidInput_shouldReturn400() throws Exception {
        ProductCreateDto invalidDto = new ProductCreateDto(
                "", // Prázdné SKU
                "", // Prázdný název
                null,
                new BigDecimal("-10.00"), // Záporná cena
                new BigDecimal("-5.00"),
                -1, // Záporná zásoba
                true
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    @DisplayName("POST /api/v1/products - duplicitní SKU vrátí 409 CONFLICT")
    void createProduct_withDuplicateSku_shouldReturn409() throws Exception {
        ProductCreateDto createDto = new ProductCreateDto(
                "HRA-KAC-001",
                "Káča",
                null,
                new BigDecimal("45.00"),
                new BigDecimal("99.00"),
                10,
                true
        );

        when(productService.createProduct(any(ProductCreateDto.class)))
                .thenThrow(new DuplicateSkuException("HRA-KAC-001"));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Duplicitní položka")));
    }

    @Test
    @DisplayName("POST /api/v1/products - prodej pod cenou vrátí 422 UNPROCESSABLE ENTITY")
    void createProduct_withNegativeMargin_shouldReturn422() throws Exception {
        ProductCreateDto createDto = new ProductCreateDto(
                "HRA-NEW-001",
                "Hračka pod cenou",
                null,
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                10,
                true
        );

        when(productService.createProduct(any(ProductCreateDto.class)))
                .thenThrow(new BusinessValidationException("Záporná marže je v systému zakázána."));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.error", is("Porušení obchodního pravidla")));
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} - úspěšná aktualizace vrátí 200 OK")
    void updateProduct_withValidInput_shouldReturn200() throws Exception {
        ProductUpdateDto updateDto = new ProductUpdateDto(
                "Dřevěná káča velká",
                "Aktualizovaný popis",
                new BigDecimal("50.00"),
                new BigDecimal("119.00"),
                25,
                true
        );

        Product p = new Product(
                "HRA-KAC-001",
                updateDto.getName(),
                updateDto.getDescription(),
                updateDto.getPurchasePrice(),
                updateDto.getSellingPrice(),
                updateDto.getMinStockLevel()
        );
        p.setId(1L);

        when(productService.updateProduct(eq(1L), any(ProductUpdateDto.class)))
                .thenReturn(ProductResponseDto.fromEntity(p));

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Dřevěná káča velká")));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - deaktivace vrátí 204 NO CONTENT")
    void deleteProduct_shouldReturn204() throws Exception {
        doNothing().when(productService).deactivateProduct(1L);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/products/alerts/low-stock - vrátí seznam položek se statusem 200 OK")
    void getLowStockAlerts_shouldReturnList() throws Exception {
        when(productService.getLowStockAlerts()).thenReturn(List.of(createSampleDto()));

        mockMvc.perform(get("/api/v1/products/alerts/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("POST /api/v1/products/{id}/order-supply - úspěšné doobjednání vrátí 200 OK")
    void orderSupply_withValidQuantity_shouldReturn200() throws Exception {
        ProductResponseDto dto = createSampleDto();
        dto.setIncomingQuantity(20);
        dto.setStockStatus("ON_ORDER");

        when(productService.orderSupply(eq(1L), eq(20))).thenReturn(dto);

        cz.uhk.fim.ppro.warehouse.dto.SupplyOrderDto orderDto = new cz.uhk.fim.ppro.warehouse.dto.SupplyOrderDto(20);

        mockMvc.perform(post("/api/v1/products/1/order-supply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incomingQuantity", is(20)))
                .andExpect(jsonPath("$.stockStatus", is("ON_ORDER")));
    }

    @Test
    @DisplayName("POST /api/v1/products/{id}/receive-supply - úspěšné naskladnění vrátí 200 OK")
    void receiveSupply_withValidData_shouldReturn200() throws Exception {
        ProductResponseDto dto = createSampleDto();
        dto.setTotalQuantity(35);
        dto.setIncomingQuantity(5);

        when(productService.receiveSupply(eq(1L), eq(1L), eq(15))).thenReturn(dto);

        cz.uhk.fim.ppro.warehouse.dto.SupplyReceiptDto receiptDto = new cz.uhk.fim.ppro.warehouse.dto.SupplyReceiptDto(1L, 15);

        mockMvc.perform(post("/api/v1/products/1/receive-supply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receiptDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity", is(35)))
                .andExpect(jsonPath("$.incomingQuantity", is(5)));
    }
}

