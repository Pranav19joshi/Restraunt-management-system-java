package exception;

public class OrderException extends Exception {
    private String orderId;

    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, String orderId) {
        super(message);
        this.orderId = orderId;
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getOrderId() {
        return orderId;
    }
}
