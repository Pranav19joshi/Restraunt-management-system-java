package exception;

public class PaymentException extends Exception {
    private double amount;

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, double amount) {
        super(message);
        this.amount = amount;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public double getAmount() {
        return amount;
    }
}
