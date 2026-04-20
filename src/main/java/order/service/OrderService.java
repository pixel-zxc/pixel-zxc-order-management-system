package order.service;

import order.exception.OutOfStockException;
import order.models.Customer;
import order.models.Order;
import order.models.OrderStatus;
import order.models.Product;

import order.repository.OrderRepository;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class OrderService {
    private final OrderRepository repository;
    private final ProductService productService;
    private final AtomicLong generateId = new AtomicLong(1);

    public OrderService(OrderRepository repository, ProductService service){
        if(service == null)
            throw new IllegalArgumentException("Service can't be null pointer");
        if(repository==null)
            throw new IllegalArgumentException("Repository can't be null pointer");
        this.repository = repository;
        this.productService=service;

    }

    public Order createOrder(Customer customer, Map<Product, Integer> products) {
        if (customer == null || products == null) {
            throw new IllegalArgumentException("Customer and products cannot be null");
        }
        for (Map.Entry<Product, Integer> entry : products.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            if (!productService.checkAvailability(product.getId(), quantity)) {
                throw new OutOfStockException(
                        "Not enough stock",
                        product.getId(),
                        quantity,
                        product.getStock()
                );
            }
        }

        for (Map.Entry<Product, Integer> entry : products.entrySet()) {
            productService.decrementStock(entry.getKey().getId(), entry.getValue());
        }

        Order order = new Order(generateId.getAndIncrement(), products, OrderStatus.CREATED);
        repository.addOrder(order);
        customer.addOrder(order);

        return order;
    }

    public void payOrder(long id){
        Optional<Order> canPay = repository.findOrderById(id);
        if(canPay.isEmpty())
            throw new IllegalArgumentException("Order not found");
        Order order = canPay.get();
        if(order.getStatus()!=OrderStatus.CREATED)
            throw new IllegalStateException("Order cannot be paid: current status is " + order.getStatus());
        order.setStatus(OrderStatus.PAID);
        repository.updateOrder(order);
    }

    public void cancelOrder(long id) {
        Optional<Order> optionalOrder = repository.findOrderById(id);
        if (optionalOrder.isEmpty())
            throw new IllegalArgumentException("Order not found");

        Order order = optionalOrder.get();
        if (order.getStatus() == OrderStatus.DELIVERED)
            throw new IllegalStateException("Cannot cancel delivered order");

        for (Map.Entry<Product, Integer> entry : order.getProducts().entrySet()) {
            productService.incrementStock(entry.getKey().getId(), entry.getValue());
        }
        order.setStatus(OrderStatus.CANCELLED);
        repository.updateOrder(order);
    }

    public List<Order> findOrdersByCustomer(Customer customer, Predicate<Order> condition){
        return customer.getOrders().stream()
                .filter(condition)
                .collect(Collectors.toList());
    }

    public double calculateTotal(Order order, UnaryOperator<Double> discountStrategy) {
        double subTotal = order.getProducts()
                .entrySet()
                .stream()
                .mapToDouble(entry -> entry.getKey().getPrice()*entry.getValue())
                .sum();
        return discountStrategy.apply(subTotal);
    }
}
