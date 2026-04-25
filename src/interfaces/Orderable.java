package interfaces;

import enums.OrderStatus;
import java.util.List;

public interface Orderable {
    String getOrderId();
    OrderStatus getStatus();
    void setStatus(OrderStatus status);
    double getOrderTotal();
    List<String> getItemNames();
}
