package cz.uhk.fim.ppro.warehouse.service;

import cz.uhk.fim.ppro.warehouse.dto.ProductCreateDto;
import cz.uhk.fim.ppro.warehouse.dto.ProductResponseDto;
import cz.uhk.fim.ppro.warehouse.dto.ProductUpdateDto;
import cz.uhk.fim.ppro.warehouse.exception.BusinessValidationException;
import cz.uhk.fim.ppro.warehouse.exception.DuplicateSkuException;
import cz.uhk.fim.ppro.warehouse.exception.ResourceNotFoundException;
import cz.uhk.fim.ppro.warehouse.model.Category;
import cz.uhk.fim.ppro.warehouse.model.Product;
import cz.uhk.fim.ppro.warehouse.model.StockItem;
import cz.uhk.fim.ppro.warehouse.model.Warehouse;
import cz.uhk.fim.ppro.warehouse.repository.CategoryRepository;
import cz.uhk.fim.ppro.warehouse.repository.ProductRepository;
import cz.uhk.fim.ppro.warehouse.repository.StockItemRepository;
import cz.uhk.fim.ppro.warehouse.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementace servisní byznys logiky pro správu katalogu produktů {@link ProductService}.
 * <p>
 * Ošetřuje klíčové doménové invarianty Dřevěnky s.r.o.:
 * <ul>
 *   <li><b>Zamezení duplicitního SKU:</b> Každá hračka musí mít unikátní identifikátor v regálu.</li>
 *   <li><b>Pozitivní obchodní marže:</b> Prodejní cena musí být vyšší nebo rovna nákupní ceně.</li>
 *   <li><b>Zboží na cestě od dodavatele:</b> Eviduje doobjednávky u truhláře pro zklidnění stavu nízkých zásob.</li>
 *   <li><b>Soft-delete:</b> Vyřazené hračky zůstávají v databázi pro historii skladových pohybů a objednávek.</li>
 * </ul>
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              StockItemRepository stockItemRepository,
                              WarehouseRepository warehouseRepository,
                              CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.stockItemRepository = stockItemRepository;
        this.warehouseRepository = warehouseRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<ProductResponseDto> getAllProducts(boolean includeInactive) {
        log.debug("Načítání seznamu produktů (includeInactive={})", includeInactive);

        List<Product> products = includeInactive
                ? productRepository.findAll()
                : productRepository.findByIsActiveTrueOrderByNameAsc();

        return products.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ProductResponseDto getProductById(Long id) {
        log.debug("Vyhledání produktu s ID: {}", id);
        Product product = findEntityById(id);
        return toDto(product);
    }

    @Override
    public ProductResponseDto getProductBySku(String sku) {
        log.debug("Vyhledání produktu podle SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Produkt", "SKU", sku));
        return toDto(product);
    }

    @Override
    public List<ProductResponseDto> getLowStockAlerts() {
        log.debug("Vyhodnocení položek s nízkou skladovou zásobou");

        List<Product> products = productRepository.findByIsActiveTrueOrderByNameAsc();

        return products.stream()
                .map(this::toDto)
                .filter(dto -> dto.getAvailableQuantity() < dto.getMinStockLevel())
                .toList();
    }

    @Override
    public List<ProductResponseDto> getApproachingLowStockAlerts() {
        log.debug("Vyhodnocení položek blížících se k minimální zásobě");

        List<Product> products = productRepository.findByIsActiveTrueOrderByNameAsc();

        return products.stream()
                .map(this::toDto)
                .filter(dto -> {
                    int avail = dto.getAvailableQuantity();
                    int min = dto.getMinStockLevel();
                    return min > 0 && avail >= min && avail < (int) Math.ceil(min * 1.5);
                })
                .toList();
    }

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductCreateDto dto) {
        log.info("Vytváření nového produktu se SKU: '{}', název: '{}'", dto.getSku(), dto.getName());

        // 1. Ověření unikátnosti SKU kódu
        if (productRepository.existsBySku(dto.getSku())) {
            throw new DuplicateSkuException(dto.getSku());
        }

        // 2. Byznys validace cenové politiky: prodejní cena nesmí být pod nákupní cenou
        validateProfitMargin(dto.getPurchasePrice(), dto.getSellingPrice());

        // 3. Vytvoření doménové entity
        Product product = new Product(
                dto.getSku(),
                dto.getName(),
                dto.getDescription(),
                dto.getPurchasePrice(),
                dto.getSellingPrice(),
                dto.getMinStockLevel()
        );

        if (dto.getIsActive() != null) {
            product.setIsActive(dto.getIsActive());
        }

        // 4. Přiřazení kategorií (vazba M:N) – pokud jsou zadány v požadavku
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());
            product.setCategories(new java.util.HashSet<>(categories));
            log.info("Přiřazeny kategorie {} k novému produktu '{}'", dto.getCategoryIds(), dto.getName());
        }

        Product savedProduct = productRepository.save(product);
        log.info("Produkt byl úspěšně uložen s ID: {}", savedProduct.getId());

        return toDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductUpdateDto dto) {
        log.info("Aktualizace produktu s ID: {}", id);

        Product product = findEntityById(id);

        // Byznys validace cenové politiky
        validateProfitMargin(dto.getPurchasePrice(), dto.getSellingPrice());

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPurchasePrice(dto.getPurchasePrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setMinStockLevel(dto.getMinStockLevel() != null ? dto.getMinStockLevel() : 0);

        if (dto.getIsActive() != null) {
            product.setIsActive(dto.getIsActive());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Produkt ID {} byl úspěšně aktualizován", id);

        return toDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deactivateProduct(Long id) {
        log.info("Deaktivace produktu ID: {} (soft-delete)", id);
        Product product = findEntityById(id);
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.warn("Fyzické smazání produktu ID: {}", id);
        Product product = findEntityById(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductResponseDto orderSupply(Long id, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessValidationException("Počet objednávaných kusů musí být kladné číslo.");
        }

        Product product = findEntityById(id);
        int current = product.getIncomingQuantity() != null ? product.getIncomingQuantity() : 0;
        product.setIncomingQuantity(current + quantity);

        Product saved = productRepository.save(product);
        log.info("Objednáno nové zboží u dodavatele pro hračku '{}': +{} ks (celkem na cestě: {} ks)",
                saved.getName(), quantity, saved.getIncomingQuantity());

        return toDto(saved);
    }

    @Override
    @Transactional
    public ProductResponseDto receiveSupply(Long id, Long warehouseId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessValidationException("Počet naskladňovaných kusů musí být kladné číslo.");
        }

        Product product = findEntityById(id);
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Sklad", "id", warehouseId));

        // Byznys validace: nelze naskladnit více kusů, než bylo objednáno u dodavatele.
        // Zabraňuje chybě, kdy skladník omylem zadá příliš vysoké číslo při příjmu zásilky.
        int incoming = product.getIncomingQuantity() != null ? product.getIncomingQuantity() : 0;
        if (incoming > 0 && quantity > incoming) {
            throw new BusinessValidationException(String.format(
                    "Naskladňujete %d ks, ale u dodavatele bylo objednáno pouze %d ks. " +
                    "Naskladnit lze maximálně objednané množství.",
                    quantity, incoming));
        }

        // Nalezení nebo založení položky na daném skladu
        StockItem stockItem = stockItemRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
                .orElseGet(() -> new StockItem(product, warehouse, 0, 0));

        // Navýšení fyzické zásoby
        stockItem.setPhysicalQuantity(stockItem.getPhysicalQuantity() + quantity);
        stockItemRepository.save(stockItem);

        // Snížení množství na cestě
        product.setIncomingQuantity(Math.max(0, incoming - quantity));
        Product saved = productRepository.save(product);

        log.info("Naskladněno {} ks hračky '{}' na sklad '{}' (nová zásoba skladu: {} ks, na cestě zbývá: {} ks)",
                quantity, product.getName(), warehouse.getName(), stockItem.getPhysicalQuantity(), saved.getIncomingQuantity());

        return toDto(saved);
    }

    // =========================================================================
    // Pomocné privátní metody
    // =========================================================================

    /**
     * Převede entitu produktu na DTO obohacené o skutečný rozpad zásob po skladech
     * a stav objednávek na cestě.
     */
    private ProductResponseDto toDto(Product product) {
        List<StockItem> stockItems = stockItemRepository.findByProductIdWithWarehouse(product.getId());

        int physical = 0;
        int reserved = 0;
        Map<String, Integer> warehouseStock = new HashMap<>();

        for (StockItem si : stockItems) {
            int phys = si.getPhysicalQuantity() != null ? si.getPhysicalQuantity() : 0;
            int res = si.getReservedQuantity() != null ? si.getReservedQuantity() : 0;
            physical += phys;
            reserved += res;
            if (si.getWarehouse() != null) {
                warehouseStock.put(si.getWarehouse().getName(), phys);
            }
        }

        int available = Math.max(0, physical - reserved);
        return ProductResponseDto.fromEntity(product, physical, available, warehouseStock);
    }

    private Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produkt", "id", id));
    }

    /**
     * Byznys validace: E-shop Dřevěnka s.r.o. nesmí prodávat zboží se zápornou marží.
     */
    private void validateProfitMargin(BigDecimal purchasePrice, BigDecimal sellingPrice) {
        if (purchasePrice != null && sellingPrice != null) {
            if (sellingPrice.compareTo(purchasePrice) < 0) {
                throw new BusinessValidationException(String.format(
                        "Prodejní cena (%.2f Kč) nesmí být nižší než nákupní cena (%.2f Kč). Záporná marže je v systému zakázána.",
                        sellingPrice, purchasePrice
                ));
            }
        }
    }
}
