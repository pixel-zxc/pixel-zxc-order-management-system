package order.models;

import java.util.Map;

public class Order {
    private final long id;
    private final Map<Product, Integer> products;
    private OrderStatus status;

    public Order(long id, Map<Product, Integer> products, OrderStatus status) {
        this.id = id;
        this.products = products;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public Map<Product, Integer> getProducts() {
        return products;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}