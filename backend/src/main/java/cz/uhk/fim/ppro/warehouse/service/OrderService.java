package cz.uhk.fim.ppro.warehouse.service;

import cz.uhk.fim.ppro.warehouse.dto.OrderCreateDto;
import cz.uhk.fim.ppro.warehouse.dto.OrderResponseDto;

import java.util.List;

/**
 * Servisní rozhraní pro správu zákaznických objednávek e-shopu Dřevěnka s.r.o.
 *
 * <p>
 * Jde o kritický bod zadání B: implementace dvoufázového rezervačního mechanismu,
 * který zabrání prodeji neexistujícího zboží (hlavní bolest klienta).
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
public interface OrderService {

    /**
     * Vrátí seznam všech objednávek seřazených od nejnovější.
     */
    List<OrderResponseDto> getAllOrders();

    /**
     * Vrátí detail konkrétní objednávky včetně položek.
     *
     * @param id  ID objednávky
     * @throws cz.uhk.fim.ppro.warehouse.exception.ResourceNotFoundException pokud objednávka neexistuje
     */
    OrderResponseDto getOrderById(Long id);

    /**
     * Vytvoří novou zákaznickou objednávku a provede atomickou rezervaci zásoby.
     *
     * <p>
     * Průběh metody:
     * <ol>
     *   <li>Ověření existence zákazníka a všech objednaných produktů.</li>
     *   <li>Pro každou položku ověří, zda disponibilní zásoba pokryje objednané množství.</li>
     *   <li>Vygeneruje číslo objednávky (ORD-YYYY-NNNNN).</li>
     *   <li>Zafixuje aktuální prodejní ceny všech položek.</li>
     *   <li>Atomicky zvýší reserved_quantity v stock_items pro každý produkt a sklad.</li>
     *   <li>Nastaví stav objednávky na RESERVED.</li>
     * </ol>
     * </p>
     *
     * @param dto  Požadavek na vytvoření objednávky
     * @throws cz.uhk.fim.ppro.warehouse.exception.BusinessValidationException pokud zásoba nestačí
     */
    OrderResponseDto createOrder(OrderCreateDto dto);

    /**
     * Stornuje objednávku a uvolní rezervované zásoby zpět do disponibilní zásoby.
     * Povoleno pouze pro objednávky ve stavu CREATED nebo RESERVED.
     *
     * @param id  ID objednávky ke stornování
     */
    OrderResponseDto cancelOrder(Long id);
}
