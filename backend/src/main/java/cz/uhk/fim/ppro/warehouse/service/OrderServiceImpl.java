package cz.uhk.fim.ppro.warehouse.service;

import cz.uhk.fim.ppro.warehouse.dto.OrderCreateDto;
import cz.uhk.fim.ppro.warehouse.dto.OrderResponseDto;
import cz.uhk.fim.ppro.warehouse.exception.BusinessValidationException;
import cz.uhk.fim.ppro.warehouse.exception.ResourceNotFoundException;
import cz.uhk.fim.ppro.warehouse.model.*;
import cz.uhk.fim.ppro.warehouse.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementace servisní byznys logiky pro zákaznické objednávky Dřevěnky s.r.o.
 *
 * <p>
 * <b>Kritický bod zadání B:</b> Dvoufázový rezervační mechanismus, který
 * atomicky
 * blokuje zásobu při přijetí objednávky a zamezuje prodeji neexistujícího
 * zboží.
 * Jde přímo o klíčový klientův problém: "Přibližně 2× do měsíce se prodá zboží,
 * které ve skutečnosti není na skladě."
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;
    private final WarehouseRepository warehouseRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            StockItemRepository stockItemRepository,
            WarehouseRepository warehouseRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.stockItemRepository = stockItemRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public List<OrderResponseDto> getAllOrders() {
        log.debug("Načítání přehledu všech objednávek");
        return orderRepository.findAllWithCustomerOrderByCreatedAtDesc()
                .stream()
                .map(o -> OrderResponseDto.fromEntity(o, null))
                .toList();
    }

    @Override
    public OrderResponseDto getOrderById(Long id) {
        log.debug("Načítání detailu objednávky ID: {}", id);
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Objednávka", "id", id));
        List<OrderItem> items = orderItemRepository.findByOrderIdWithDetails(id);
        return OrderResponseDto.fromEntity(order, items);
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto) {
        log.info("Vytváření nové objednávky pro zákazníka ID: {}", dto.getCustomerId());

        // 1. Ověření existence zákazníka
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Zákazník", "id", dto.getCustomerId()));

        // 2. Vygenerování čísla objednávky (ORD-YYYY-NNNNN)
        int year = LocalDateTime.now().getYear();
        long count = orderRepository.countByYear(year);
        String orderNumber = String.format("ORD-%d-%05d", year, count + 1);

        // 3. Příprava entity objednávky
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomer(customer);
        order.setNote(dto.getNote());
        order.setStatus(Order.OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.ZERO);

        // 4. Zpracování položek a atomická rezervace zásoby
        BigDecimal total = BigDecimal.ZERO;

        for (OrderCreateDto.OrderItemCreateDto itemDto : dto.getItems()) {
            // Ověření existence produktu
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produkt", "id", itemDto.getProductId()));

            // Zafixování aktuální prodejní ceny v okamžiku objednávky
            BigDecimal fixedPrice = product.getSellingPrice();
            int requestedQuantity = itemDto.getQuantity();

            // Zjištění stavu zásob napříč všemi sklady
            List<StockItem> allStockItems = stockItemRepository.findByProductIdWithWarehouse(product.getId());
            int totalAvailableAcrossWarehouses = allStockItems.stream()
                    .mapToInt(si -> Math.max(0, si.getPhysicalQuantity() - si.getReservedQuantity()))
                    .sum();

            if (totalAvailableAcrossWarehouses < requestedQuantity) {
                throw new BusinessValidationException(String.format(
                        "Nedostatečná zásoba produktu '%s'. Celkem napříč všemi sklady je dostupných pouze %d ks, ale objednáváte %d ks.",
                        product.getName(), totalAvailableAcrossWarehouses, requestedQuantity));
            }

            // Určení preferovaného skladu výdeje (pokud byl zadán)
            Warehouse preferredWarehouse = null;
            if (itemDto.getWarehouseId() != null && itemDto.getWarehouseId() > 0) {
                preferredWarehouse = warehouseRepository.findById(itemDto.getWarehouseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Sklad", "id", itemDto.getWarehouseId()));
            }

            // Rozdělení odběru (Split fulfillment):
            // Pokud preferovaný sklad pokryje celou poptávku, vezmeme vše z něj.
            // Pokud ne (nebo nebyl zvolen), rozdělíme rezervaci mezi sklady podle dostupné
            // kapacity.
            int remainingToFulfill = requestedQuantity;

            // Seřazení skladů: preferovaný sklad první, jinak primárně Hlavní sklad
            // (expediční)
            final Long prefId = preferredWarehouse != null ? preferredWarehouse.getId() : null;
            List<StockItem> sortedStocks = new java.util.ArrayList<>(allStockItems);
            sortedStocks.sort((a, b) -> {
                if (prefId != null) {
                    if (a.getWarehouse().getId().equals(prefId))
                        return -1;
                    if (b.getWarehouse().getId().equals(prefId))
                        return 1;
                }
                // Jinak preferuj sklad s kódem 'HRADEC' (hlavní expediční sklad)
                if ("HRADEC".equalsIgnoreCase(a.getWarehouse().getCode()))
                    return -1;
                if ("HRADEC".equalsIgnoreCase(b.getWarehouse().getCode()))
                    return 1;
                return 0;
            });

            for (StockItem stockItem : sortedStocks) {
                if (remainingToFulfill <= 0)
                    break;

                int avail = Math.max(0, stockItem.getPhysicalQuantity() - stockItem.getReservedQuantity());
                if (avail <= 0)
                    continue;

                int take = Math.min(avail, remainingToFulfill);

                // Atomická rezervace zásoby na daném skladu
                stockItem.setReservedQuantity(stockItem.getReservedQuantity() + take);
                stockItemRepository.save(stockItem);

                // Vytvoření položky objednávky pro daný sklad
                OrderItem orderItem = new OrderItem(order, product, stockItem.getWarehouse(), take, fixedPrice);
                order.getItems().add(orderItem);

                total = total.add(fixedPrice.multiply(BigDecimal.valueOf(take)));
                remainingToFulfill -= take;

                log.info("Rezervováno {} ks '{}' na skladu '{}' pro objednávku '{}'",
                        take, product.getName(), stockItem.getWarehouse().getName(), orderNumber);
            }

            if (remainingToFulfill > 0) {
                // Nemělo by nastat díky předchozí kontrole totalAvailableAcrossWarehouses
                throw new BusinessValidationException(String.format(
                        "Neočekávaná chyba při rezervaci produktu '%s'. Chybí %d ks.",
                        product.getName(), remainingToFulfill));
            }
        }

        order.setTotalPrice(total);
        // Po úspěšné rezervaci přechází objednávka rovnou do stavu RESERVED (dvoufázový
        // model)
        order.setStatus(Order.OrderStatus.RESERVED);

        Order saved = orderRepository.save(order);
        log.info("Objednávka '{}' úspěšně vytvořena ve stavu '{}', celková cena: {} Kč",
                saved.getOrderNumber(), saved.getStatus(), saved.getTotalPrice());

        List<OrderItem> savedItems = orderItemRepository.findByOrderIdWithDetails(saved.getId());
        return OrderResponseDto.fromEntity(saved, savedItems);
    }

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long id) {
        log.info("Stornování objednávky ID: {}", id);

        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Objednávka", "id", id));

        // Storno je povoleno pouze pro CREATED a RESERVED
        if (order.getStatus() == Order.OrderStatus.PROCESSING ||
                order.getStatus() == Order.OrderStatus.DISPATCHED) {
            throw new BusinessValidationException(
                    String.format("Objednávku '%s' ve stavu '%s' nelze stornovat. " +
                            "Kontaktujte skladníka pro manuální řešení.",
                            order.getOrderNumber(), order.getStatus().name()));
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BusinessValidationException(
                    "Objednávka je již stornována.");
        }

        // Uvolnění rezervovaných zásob zpět do disponibilní zásoby
        if (order.getStatus() == Order.OrderStatus.RESERVED) {
            List<OrderItem> items = orderItemRepository.findByOrderIdWithDetails(id);
            for (OrderItem item : items) {
                if (item.getWarehouse() != null) {
                    stockItemRepository.findByProductIdAndWarehouseId(
                            item.getProduct().getId(), item.getWarehouse().getId())
                            .ifPresent(si -> {
                                int newReserved = Math.max(0, si.getReservedQuantity() - item.getQuantity());
                                si.setReservedQuantity(newReserved);
                                stockItemRepository.save(si);
                                log.info("Uvolněna rezervace {} ks '{}' ze skladu '{}'",
                                        item.getQuantity(), item.getProduct().getName(),
                                        item.getWarehouse().getName());
                            });
                }
            }
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        List<OrderItem> savedItems = orderItemRepository.findByOrderIdWithDetails(saved.getId());
        return OrderResponseDto.fromEntity(saved, savedItems);
    }
}
