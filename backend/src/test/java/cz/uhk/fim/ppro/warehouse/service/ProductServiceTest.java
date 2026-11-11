package cz.uhk.fim.ppro.warehouse.service;

import cz.uhk.fim.ppro.warehouse.dto.ProductCreateDto;
import cz.uhk.fim.ppro.warehouse.dto.ProductResponseDto;
import cz.uhk.fim.ppro.warehouse.dto.ProductUpdateDto;
import cz.uhk.fim.ppro.warehouse.exception.BusinessValidationException;
import cz.uhk.fim.ppro.warehouse.exception.DuplicateSkuException;
import cz.uhk.fim.ppro.warehouse.exception.ResourceNotFoundException;
import cz.uhk.fim.ppro.warehouse.model.Product;
import cz.uhk.fim.ppro.warehouse.model.StockItem;
import cz.uhk.fim.ppro.warehouse.model.Warehouse;
import cz.uhk.fim.ppro.warehouse.repository.ProductRepository;
import cz.uhk.fim.ppro.warehouse.repository.StockItemRepository;
import cz.uhk.fim.ppro.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Jednotkové (Unit) testy pro servisní byznys vrstvu {@link ProductServiceImpl}.
 * <p>
 * Ověřuje klíčová obchodní pravidla Dřevěnky s.r.o. bez nutnosti běžící databáze.
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockItemRepository stockItemRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product(
                "HRA-KAC-001",
                "Dřevěná káča pestrobarevná",
                "Tradiční buková káča",
                new BigDecimal("45.00"),
                new BigDecimal("99.00"),
                15
        );
        sampleProduct.setId(1L);
        sampleProduct.setIncomingQuantity(0);
    }

    @Test
    @DisplayName("Úspěšné vytvoření nové hračky s validními údaji")
    void createProduct_withValidData_shouldSucceed() {
        ProductCreateDto dto = new ProductCreateDto(
                "HRA-KAC-002",
                "Dřevěná káča přírodní",
                "Přírodní dřevo",
                new BigDecimal("35.00"),
                new BigDecimal("79.00"),
                10,
                true
        );

        when(productRepository.existsBySku("HRA-KAC-002")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });
        when(stockItemRepository.findByProductIdWithWarehouse(2L)).thenReturn(Collections.emptyList());

        ProductResponseDto result = productService.createProduct(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getSku()).isEqualTo("HRA-KAC-002");
        assertThat(result.getName()).isEqualTo("Dřevěná káča přírodní");
        assertThat(result.getMarginAmount()).isEqualByComparingTo("44.00");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Odmítnutí vytvoření hračky s již existujícím SKU kódem")
    void createProduct_withDuplicateSku_shouldThrowDuplicateSkuException() {
        ProductCreateDto dto = new ProductCreateDto(
                "HRA-KAC-001",
                "Káča",
                null,
                new BigDecimal("40.00"),
                new BigDecimal("80.00"),
                5,
                true
        );

        when(productRepository.existsBySku("HRA-KAC-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(dto))
                .isInstanceOf(DuplicateSkuException.class)
                .hasMessageContaining("HRA-KAC-001");
    }

    @Test
    @DisplayName("Odmítnutí záporné marže (prodejní cena nižší než nákupní)")
    void createProduct_withSellingPriceLowerThanPurchasePrice_shouldThrowBusinessValidationException() {
        ProductCreateDto dto = new ProductCreateDto(
                "HRA-TEST-001",
                "Testovací hračka",
                null,
                new BigDecimal("100.00"),
                new BigDecimal("80.00"),
                5,
                true
        );

        when(productRepository.existsBySku("HRA-TEST-001")).thenReturn(false);

        assertThatThrownBy(() -> productService.createProduct(dto))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Záporná marže je v systému zakázána");
    }

    @Test
    @DisplayName("Vyhledání produktu podle platného ID")
    void getProductById_withExistingId_shouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(stockItemRepository.findByProductIdWithWarehouse(1L)).thenReturn(Collections.emptyList());

        ProductResponseDto result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSku()).isEqualTo("HRA-KAC-001");
        assertThat(result.getName()).isEqualTo("Dřevěná káča pestrobarevná");
    }

    @Test
    @DisplayName("Vyhledání produktu s neexistujícím ID vyvolá ResourceNotFoundException")
    void getProductById_withNonExistingId_shouldThrowResourceNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Úspěšná aktualizace parametrů produktu")
    void updateProduct_withValidData_shouldUpdateAndReturn() {
        ProductUpdateDto updateDto = new ProductUpdateDto(
                "Dřevěná káča nová",
                "Nový popis",
                new BigDecimal("50.00"),
                new BigDecimal("120.00"),
                20,
                true
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        when(stockItemRepository.findByProductIdWithWarehouse(1L)).thenReturn(Collections.emptyList());

        ProductResponseDto result = productService.updateProduct(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(sampleProduct.getName()).isEqualTo("Dřevěná káča nová");
        assertThat(sampleProduct.getPurchasePrice()).isEqualByComparingTo("50.00");
        assertThat(sampleProduct.getSellingPrice()).isEqualByComparingTo("120.00");
        assertThat(sampleProduct.getMinStockLevel()).isEqualTo(20);
    }

    @Test
    @DisplayName("Deaktivace produktu nastaví isActive na false (soft-delete)")
    void deactivateProduct_shouldSetIsActiveFalse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        productService.deactivateProduct(1L);

        assertThat(sampleProduct.getIsActive()).isFalse();
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("Zadání doobjednávky u truhláře (zboží na cestě)")
    void orderSupply_shouldIncreaseIncomingQuantity() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        when(stockItemRepository.findByProductIdWithWarehouse(1L)).thenReturn(Collections.emptyList());

        ProductResponseDto result = productService.orderSupply(1L, 25);

        assertThat(result).isNotNull();
        assertThat(sampleProduct.getIncomingQuantity()).isEqualTo(25);
        assertThat(result.getIncomingQuantity()).isEqualTo(25);
        // Protože physical=0 < min=15, ale incoming=25 > 0 -> stav je ON_ORDER
        assertThat(result.getStockStatus()).isEqualTo("ON_ORDER");
        assertThat(result.getStockStatusLabel()).contains("Na cestě (25 ks)");
    }

    @Test
    @DisplayName("Naskladnění dodávky navýší skladovou zásobu a sníží množství na cestě")
    void receiveSupply_shouldIncreasePhysicalStockAndDecreaseIncoming() {
        sampleProduct.setIncomingQuantity(20);
        Warehouse warehouse = new Warehouse("HRADEC", "Hlavní sklad", "Brněnská");
        warehouse.setId(1L);
        StockItem stockItem = new StockItem(sampleProduct, warehouse, 10, 0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(stockItemRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(stockItem));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        when(stockItemRepository.findByProductIdWithWarehouse(1L)).thenReturn(List.of(stockItem));

        ProductResponseDto result = productService.receiveSupply(1L, 1L, 15);

        assertThat(result).isNotNull();
        assertThat(stockItem.getPhysicalQuantity()).isEqualTo(25); // 10 + 15
        assertThat(sampleProduct.getIncomingQuantity()).isEqualTo(5);  // 20 - 15
        verify(stockItemRepository).save(stockItem);
    }
}
