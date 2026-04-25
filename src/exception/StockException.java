package exception;

public class StockException extends RuntimeException {
    private String itemName;

    public StockException(String message) {
        super(message);
    }

    public StockException(String message, String itemName) {
        super(message);
        this.itemName = itemName;
    }

    public StockException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getItemName() {
        return itemName;
    }
}
