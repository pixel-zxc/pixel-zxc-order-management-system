package order.models;

import java.util.Objects;

public class Product {
    private final long id;
    private final String name;
    private double price;
    private int stock;

    public Product(long id, String name, double price, int stock) {
        if (stock < 0)
            throw new IllegalArgumentException("Stock cannot be negative");
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        if (id < 0)
            throw new IllegalArgumentException("ID cannot be negative");
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Name cannot be null or empty");

        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public void setPrice(double price) {
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        this.price = price;
    }

    public void setStock(int stock) {
        if (stock < 0)
            throw new IllegalArgumentException("Stock cannot be negative");
        this.stock = stock;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}