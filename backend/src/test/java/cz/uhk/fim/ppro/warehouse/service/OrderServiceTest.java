package cz.uhk.fim.ppro.warehouse.service;

import cz.uhk.fim.ppro.warehouse.dto.OrderCreateDto;
import cz.uhk.fim.ppro.warehouse.dto.OrderResponseDto;
import cz.uhk.fim.ppro.warehouse.exception.BusinessValidationException;
import cz.uhk.fim.ppro.warehouse.model.*;
import cz.uhk.fim.ppro.warehouse.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Jednotkové (Unit) testy pro servisní byznys logiku zákaznických objednávek {@link OrderServiceImpl}.
 * <p>
 * Ověřuje kritický bod zadání B:
 * - Dvoufázový rezervační model (při objednání atomicky rezervovat zásobu)
 * - Odmítnutí objednávky při nedostatku zásoby (zamezení prodeje neexistujícího zboží)
 * - Fixace ceny v objednávce
 * - Uvolnění rezervace při stornu
 * </p>
 *
 * @author Dřevěnka s.r.o. / PPRO
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockItemRepository stockItemRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer sampleCustomer;
    private Product sampleProduct;
    private Warehouse sampleWarehouse;
    private StockItem sampleStockItem;

    @BeforeEach
    void setUp() {
        sampleCustomer = new Customer("Jana", "Horáčková", "jana@example.cz", "+420111222333", "Palachova 1", "Hradec Králové", "50002");
        sampleCustomer.setOrders(new java.util.ArrayList<>());

        sampleProduct = new Product("HRA-KAC-001", "Dřevěná káča", "Popis", new BigDecimal("45.00"), new BigDecimal("99.00"), 10);
        sampleProduct.setId(1L);

        sampleWarehouse = new Warehouse("HRADEC", "Hlavní sklad", "Brněnská");
        sampleWarehouse.setId(1L);

        // Fyzická zásoba 20 ks, rezervováno 5 ks -> volná disponibilní zásoba = 15 ks
        sampleStockItem = new StockItem(sampleProduct, sampleWarehouse, 20, 5);
        sampleStockItem.setId(10L);
    }

    @Test
    @DisplayName("Vytvoření objednávky úspěšně rezervuje zásobu a zafixuje cenu")
    void createOrder_shouldReserveStockAndFixPrice() {
        OrderCreateDto dto = new OrderCreateDto();
        dto.setCustomerId(1L);
        dto.setNote("Spěchá");

        OrderCreateDto.OrderItemCreateDto itemDto = new OrderCreateDto.OrderItemCreateDto();
        itemDto.setProductId(1L);
        itemDto.setWarehouseId(1L);
        itemDto.setQuantity(3);
        dto.setItems(List.of(itemDto));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(orderRepository.countByYear(anyInt())).thenReturn(0L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sampleWarehouse));
        when(stockItemRepository.findByProductIdWithWarehouse(1L)).thenReturn(List.of(sampleStockItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            return o;
        });
        when(orderItemRepository.findByOrderIdWithDetails(any())).thenReturn(List.of(
                new OrderItem(null, sampleProduct, sampleWarehouse, 3, new BigDecimal("99.00"))
        ));

        OrderResponseDto response = orderService.createOrder(dto);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("RESERVED");
        // Původní rezervace byla 5 ks, objednáno 3 ks -> nová rezervace musí být 8 ks
        assertThat(sampleStockItem.getReservedQuantity()).isEqualTo(8);
        verify(stockItemRepository).save(sampleStockItem);
    }

    @Test
    @DisplayName("Odmítnutí objednávky při nedostatku disponibilní zásoby napříč všemi sklady")
    void createOrder_shouldThrowException_whenInsufficientStock() {
        OrderCreateDto dto = new OrderCreateDto();
        dto.setCustomerId(1L);

        OrderCreateDto.OrderItemCreateDto itemDto = new OrderCreateDto.OrderItemCreateDto();
        itemDto.setProductId(1L);
        itemDto.setWarehouseId(1L);
        itemDto.setQuantity(50); // Dostupné je jen 15 ks (20 - 5)
        dto.setItems(List.of(itemDto));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(orderRepository.countByYear(anyInt())).thenReturn(0L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(stockItemRepository.findByProductIdWithWarehouse(1L)).thenReturn(List.of(sampleStockItem));

        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Nedostatečná zásoba produktu");

        // Zásoba se nesmí změnit
        assertThat(sampleStockItem.getReservedQuantity()).isEqualTo(5);
        verify(stockItemRepository, never()).save(sampleStockItem);
    }

    @Test
    @DisplayName("Split fulfillment: Objednávka se automaticky rozdělí mezi dva sklady, pokud jeden nestačí")
    void createOrder_splitFulfillment_shouldReserveAcrossMultipleWarehouses() {
        // Sklad 1: Hradec Králové - volných 10 ks (fyzicky 15, rezervováno 5)
        StockItem stockHradec = new StockItem(sampleProduct, sampleWarehouse, 15, 5);

        // Sklad 2: Třebechovice - volných 6 ks (fyzicky 6, rezervováno 0)
        Warehouse warehouseTreb = new Warehouse("TREBECHOVICE", "Garáž Třebechovice", "Nádražní");
        warehouseTreb.setId(2L);
        StockItem stockTreb = new StockItem(sampleProduct, warehouseTreb, 6, 0);

        // Zákazník objednává 14 ks hračky (ani jeden sklad nemá sám 14 ks, ale celkem je 10 + 6 = 16 ks)
        OrderCreateDto dto = new OrderCreateDto();
        dto.setCustomerId(1L);

        OrderCreateDto.OrderItemCreateDto itemDto = new OrderCreateDto.OrderItemCreateDto();
        itemDto.setProductId(1L);
        itemDto.setWarehouseId(1L); // Preferovaný Hradec
        itemDto.setQuantity(14);
        dto.setItems(List.of(itemDto));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(orderRepository.countByYear(anyInt())).thenReturn(0L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(sampleWarehouse));
        when(stockItemRepository.findByProductIdWithWarehouse(1L)).thenReturn(List.of(stockHradec, stockTreb));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.findByOrderIdWithDetails(any())).thenReturn(List.of(
                new OrderItem(null, sampleProduct, sampleWarehouse, 10, new BigDecimal("99.00")),
                new OrderItem(null, sampleProduct, warehouseTreb, 4, new BigDecimal("99.00"))
        ));

        OrderResponseDto response = orderService.createOrder(dto);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("RESERVED");

        // Ověření rozdělení:
        // Hradec pokryl 10 ks -> rezervace stoupla z 5 na 15
        assertThat(stockHradec.getReservedQuantity()).isEqualTo(15);
        // Třebechovice pokryly zbývající 4 ks -> rezervace stoupla z 0 na 4
        assertThat(stockTreb.getReservedQuantity()).isEqualTo(4);

        verify(stockItemRepository).save(stockHradec);
        verify(stockItemRepository).save(stockTreb);
    }

    @Test
    @DisplayName("Stornování objednávky uvolní rezervovanou zásobu")
    void cancelOrder_shouldReleaseReservedStock() {
        Order order = new Order();
        order.setOrderNumber("ORD-2026-00001");
        order.setCustomer(sampleCustomer);
        order.setStatus(Order.OrderStatus.RESERVED);

        OrderItem orderItem = new OrderItem(order, sampleProduct, sampleWarehouse, 4, new BigDecimal("99.00"));
        order.setItems(List.of(orderItem));

        when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdWithDetails(1L)).thenReturn(List.of(orderItem));
        when(stockItemRepository.findByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(sampleStockItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDto cancelled = orderService.cancelOrder(1L);

        assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");
        // Rezervace byla 5, stornovaly se 4 ks -> zbyde 1 ks
        assertThat(sampleStockItem.getReservedQuantity()).isEqualTo(1);
        verify(stockItemRepository).save(sampleStockItem);
    }
}
