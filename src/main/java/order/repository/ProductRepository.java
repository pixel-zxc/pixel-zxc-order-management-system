package order.repository;

import order.models.Product;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProductRepository {
    private final Map<Long, Product> products;
    public ProductRepository(Map<Long, Product> products) {
        this.products = products;
    }

    public  ProductRepository(){
        this.products = new HashMap<>();
    }

    public void addProduct(Product product) {
        if (products.containsKey(product.getId()))
            throw new IllegalArgumentException("Product already contains");
        products.put(product.getId(), product);
    }

    public void removeProduct(long id) {
        products.remove(id);
    }


    public Optional<Product> findProductById(long id) {
        return Optional.ofNullable(products.get(id));
    }

    public void updateProduct(Product product) {
        if (!products.containsKey(product.getId()))
            throw new IllegalArgumentException("Product didn't constants");
        products.put(product.getId(),product);
    }
}
