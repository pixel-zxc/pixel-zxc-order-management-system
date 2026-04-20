package order.models;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private final long id;
    private String email;
    private final List<Order> orders = new ArrayList<>();

    public Customer(long id, String email) {
        if(id <0)
            throw new IllegalArgumentException("Id can't be a negative");
        this.id = id;
        if(null == email || email.isEmpty())
            throw new IllegalArgumentException("Email can't be a null or empty");
        this.email = email;

    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setEmail(String email) {
        if(email == null||email.isEmpty())
            throw new IllegalArgumentException("Empty value cannot be set.");
        this.email = email;
    }
    public void addOrder(Order order){
        if(order == null)
            throw new IllegalArgumentException("Order cannot be a null");
        orders.add(order);
    }
}
