package interfaces;

public interface Payable {
    double getTotal();
    boolean isPaid();
    void markAsPaid();
}
