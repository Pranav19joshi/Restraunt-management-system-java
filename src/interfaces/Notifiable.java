package interfaces;

public interface Notifiable {
    void onOrderReady(String orderId, int tableNumber);
    void onOrderCancelled(String orderId);
}
