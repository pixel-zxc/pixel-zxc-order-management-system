package order.repository;

import order.models.Order;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OrderRepository {
    private final Map<Long, Order> orders;

    public OrderRepository(){
        orders = new HashMap<>();
    }

    public OrderRepository(Map<Long, Order> orders){
        if(orders == null)
            throw new NullPointerException("Orders can't be null");
        this.orders = orders;
    }

    public void addOrder(Order order){
        if(orders.containsKey(order.getId()))
            throw new IllegalArgumentException("Order already contains");
        orders.put(order.getId(),order);
    }

    public void removeOrder(long id){
        orders.remove(id);
    }

    public Optional<Order> findOrderById(long id){
        return Optional.ofNullable(orders.get(id));
    }

    public void updateOrder(Order order){
        if(!orders.containsKey(order.getId()))
            throw new IllegalArgumentException("Order can't contains");
        orders.put(order.getId(),order);
    }

    public Map<Long, Order> getOrders() {
        return orders;
    }
}
