package order.service;

import order.exception.OutOfStockException;
import order.repository.ProductRepository;
import order.models.Product;

public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public boolean checkAvailability(long id, int neededStocks) {
        return repository.findProductById(id)
                .map(p -> p.getStock() >= neededStocks)
                .orElse(false);
    }

    public void incrementStock(long id, int count) {
        Product product = repository.findProductById(id).orElseThrow(()-> new IllegalArgumentException("Product not found"));
        product.setStock(product.getStock() + count);
        repository.updateProduct(product);
    }

    public void decrementStock(long id, int count) {
        Product product = repository.findProductById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!checkAvailability(id, count))
            throw new OutOfStockException(
                    "Product not available",
                    id,
                    count,
                    repository.findProductById(id)
                            .orElseThrow()
                            .getStock()
            );
        product.setStock(product.getStock() - count);
        repository.updateProduct(product);
    }

}