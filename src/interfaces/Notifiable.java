package interfaces;

public interface Notifiable {
    void onOrderReady(String orderId);
    void onOrderCancelled(String orderId);
}
