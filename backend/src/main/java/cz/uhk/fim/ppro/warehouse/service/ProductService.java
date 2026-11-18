package cz.uhk.fim.ppro.warehouse.service;

import cz.uhk.fim.ppro.warehouse.dto.ProductCreateDto;
import cz.uhk.fim.ppro.warehouse.dto.ProductResponseDto;
import cz.uhk.fim.ppro.warehouse.dto.ProductUpdateDto;

import java.util.List;

/**
 * Servisní rozhraní definující byznys operace pro správu katalogu produktů.
 * <p>
 * Představuje aplikační a byznys vrstvu systému. Zapouzdřuje obchodní pravidla,
 * kalkulace marží, validace unikátnosti a pravidla skladové dostupnosti.
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
public interface ProductService {

    /**
     * Získá seznam všech produktů v katalogu.
     *
     * @param includeInactive pokud je {@code true}, vrátí i vyřazené/deaktivované položky
     * @return seznam produktů reprezentovaných jako {@link ProductResponseDto}
     */
    List<ProductResponseDto> getAllProducts(boolean includeInactive);

    /**
     * Vyhledá konkrétní produkt podle jeho primárního klíče (ID).
     *
     * @param id primární identifikátor produktu
     * @return detail produktu
     * @throws cz.uhk.fim.ppro.warehouse.exception.ResourceNotFoundException pokud produkt neexistuje
     */
    ProductResponseDto getProductById(Long id);

    /**
     * Vyhledá produkt podle unikátního skladového kódu (SKU).
     *
     * @param sku skladový kód hračky
     * @return detail produktu
     * @throws cz.uhk.fim.ppro.warehouse.exception.ResourceNotFoundException pokud produkt neexistuje
     */
    ProductResponseDto getProductBySku(String sku);

    /**
     * Vrátí seznam položek, u kterých je zásoba pod minimální bezpečnou hranicí
     * (prioritní dashboard sekce "co hoří").
     *
     * @return seznam ohrožených produktů vyžadujících doobjednání
     */
    List<ProductResponseDto> getLowStockAlerts();

    /**
     * Vrátí seznam položek, které se blíží k minimální zásobě (zásoba je mezi minimem
     * a 1.5× minima). Umožňuje skladníkovi včas plánovat objednávky u truhláře.
     *
     * @return seznam produktů blížících se k minimu
     */
    List<ProductResponseDto> getApproachingLowStockAlerts();

    /**
     * Zaregistruje nový produkt do katalogu Dřevěnky s.r.o.
     * Provádí byznys validaci unikátnosti SKU a nezáporné marže.
     *
     * @param dto vstupní data nového produktu
     * @return vytvořený produkt s vygenerovaným ID
     * @throws cz.uhk.fim.ppro.warehouse.exception.DuplicateSkuException        pokud SKU již existuje
     * @throws cz.uhk.fim.ppro.warehouse.exception.BusinessValidationException   pokud je prodejní cena nižší než nákupní
     */
    ProductResponseDto createProduct(ProductCreateDto dto);

    /**
     * Aktualizuje parametry existujícího produktu (ceny, popis, minimální zásobu).
     *
     * @param id  identifikátor upravovaného produktu
     * @param dto nová data produktu
     * @return aktualizovaný produkt
     * @throws cz.uhk.fim.ppro.warehouse.exception.ResourceNotFoundException   pokud produkt neexistuje
     * @throws cz.uhk.fim.ppro.warehouse.exception.BusinessValidationException pokud je prodejní cena nižší než nákupní
     */
    ProductResponseDto updateProduct(Long id, ProductUpdateDto dto);

    /**
     * Deaktivuje produkt z nabídky katalogu (soft-delete).
     * Fyzický záznam zůstává v DB pro zachování integrity historických objednávek.
     *
     * @param id identifikátor produktu
     * @throws cz.uhk.fim.ppro.warehouse.exception.ResourceNotFoundException pokud produkt neexistuje
     */
    void deactivateProduct(Long id);

    /**
     * Smaže produkt z databáze, pokud na něj neodkazují žádné historické vazby.
     *
     * @param id identifikátor produktu
     * @throws cz.uhk.fim.ppro.warehouse.exception.ResourceNotFoundException pokud produkt neexistuje
     */
    void deleteProduct(Long id);

    /**
     * Zadá objednávku nového zboží u truhláře / dodavatele (zboží na cestě).
     *
     * @param id       identifikátor produktu
     * @param quantity počet objednávaných kusů
     * @return aktualizovaný produkt se zvýšeným {@code incomingQuantity} a zklidněným statusem
     */
    ProductResponseDto orderSupply(Long id, Integer quantity);

    /**
     * Naskladní doručené zboží od dodavatele na zvolený sklad a sníží množství na cestě.
     *
     * @param id          identifikátor produktu
     * @param warehouseId identifikátor skladu pro příjem (1 = Hradec, 2 = Třebechovice)
     * @param quantity    počet přijatých kusů
     * @return aktualizovaný produkt s navýšenou fyzickou zásobou
     */
    ProductResponseDto receiveSupply(Long id, Long warehouseId, Integer quantity);
}

