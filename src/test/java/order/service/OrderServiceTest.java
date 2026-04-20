package order.service;

import order.exception.OutOfStockException;
import order.models.Customer;
import order.models.Order;
import order.models.OrderStatus;
import order.models.Product;
import order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Product laptop;
    private Product mouse;
    private Map<Product, Integer> cart;

    @BeforeEach
    void setUp() {
        customer = new Customer(1L, "customer@example.com");
        laptop = new Product(1L, "Laptop", 1200.0, 15);
        mouse = new Product(2L, "Mouse", 25.0, 10);

        cart = new HashMap<>();
        cart.put(laptop, 1);
        cart.put(mouse, 2);
    }

    @Test
    @DisplayName("1. Create order successfully when stock is sufficient")
    void createOrder_Success() {
        when(productService.checkAvailability(1L, 1)).thenReturn(true);
        when(productService.checkAvailability(2L, 2)).thenReturn(true);

        Order order = orderService.createOrder(customer, cart);

        assertNotNull(order);
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(2, order.getProducts().size());

        verify(productService).decrementStock(1L, 1);
        verify(productService).decrementStock(2L, 2);
        verify(orderRepository).addOrder(any(Order.class));
        assertEquals(1, customer.getOrders().size());
    }

    @Test
    @DisplayName("2. Throw exception when customer is null")
    void createOrder_ThrowsException_WhenCustomerIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(null, cart));
        verify(productService, never()).decrementStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("3. Throw exception when products map is null")
    void createOrder_ThrowsException_WhenProductsIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(customer, null));
    }

    @Test
    @DisplayName("4. Throw OutOfStockException when stock is insufficient")
    void createOrder_ThrowsOutOfStockException_WhenStockInsufficient() {
        when(productService.checkAvailability(1L, 1)).thenReturn(true);
        when(productService.checkAvailability(2L, 2)).thenReturn(false);

        assertThrows(OutOfStockException.class,
                () -> orderService.createOrder(customer, cart));

        verify(productService, never()).decrementStock(anyLong(), anyInt());
        verify(orderRepository, never()).addOrder(any());
    }

    @Test
    @DisplayName("5. Pay order successfully when status is CREATED")
    void payOrder_Success() {
        Order order = new Order(1L, cart, OrderStatus.CREATED);
        when(orderRepository.findOrderById(1L)).thenReturn(Optional.of(order));

        orderService.payOrder(1L);

        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(orderRepository).updateOrder(order);
    }

    @Test
    @DisplayName("6. Throw exception when order not found")
    void payOrder_ThrowsException_WhenOrderNotFound() {
        when(orderRepository.findOrderById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.payOrder(999L));
        verify(orderRepository, never()).updateOrder(any());
    }

    @Test
    @DisplayName("7. Throw exception when order status is not CREATED")
    void payOrder_ThrowsException_WhenOrderStatusIsNotCreated() {
        Order order = new Order(1L, cart, OrderStatus.PAID);
        when(orderRepository.findOrderById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.payOrder(1L));
        verify(orderRepository, never()).updateOrder(any());
    }

    @Test
    @DisplayName("8. Cancel order successfully and return stock")
    void cancelOrder_Success() {
        Order order = new Order(1L, cart, OrderStatus.CREATED);
        when(orderRepository.findOrderById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(productService).incrementStock(1L, 1);
        verify(productService).incrementStock(2L, 2);
        verify(orderRepository).updateOrder(order);
    }

    @Test
    @DisplayName("9. Throw exception when cancelling delivered order")
    void cancelOrder_ThrowsException_WhenOrderIsDelivered() {
        Order order = new Order(1L, cart, OrderStatus.DELIVERED);
        when(orderRepository.findOrderById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L));
        verify(productService, never()).incrementStock(anyLong(), anyInt());
    }

    @Test
    @DisplayName("10. Find orders by customer with predicate filter")
    void findOrdersByCustomer_FiltersCorrectly() {
        Order order1 = new Order(1L, cart, OrderStatus.CREATED);
        Order order2 = new Order(2L, cart, OrderStatus.PAID);
        customer.addOrder(order1);
        customer.addOrder(order2);

        var result = orderService.findOrdersByCustomer(customer,
                o -> o.getStatus() == OrderStatus.PAID);

        assertEquals(1, result.size());
        assertEquals(OrderStatus.PAID, result.get(0).getStatus());
    }

    @Test
    @DisplayName("11. Calculate total with discount strategy")
    void calculateTotal_AppliesDiscountStrategy() {
        Order order = new Order(1L, cart, OrderStatus.CREATED);

        double totalWithDiscount = orderService.calculateTotal(order, subtotal -> subtotal * 0.9);
        double totalWithoutDiscount = orderService.calculateTotal(order, subtotal -> subtotal);

        double expectedSubtotal = 1200.0 + 25.0 * 2; // 1250
        assertEquals(expectedSubtotal, totalWithoutDiscount, 0.01);
        assertEquals(expectedSubtotal * 0.9, totalWithDiscount, 0.01);
    }
}