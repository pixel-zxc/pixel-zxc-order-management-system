package order.exception;

public class OutOfStockException extends RuntimeException {
    private final long productId;
    private final int requested;
    private final int available;

    public OutOfStockException(String message, long productId, int requested, int available) {
        super(message);
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }

    public long getProductId() {
        return productId;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}