package service;

import enums.OrderStatus;
import model.Order;
import repository.OrderRepository;

import java.util.List;

public class KitchenService {
    private final OrderRepository orderRepository;

    public KitchenService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public synchronized void advanceOrderStatus(String orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) return;

        OrderStatus current = order.getStatus();
        OrderStatus next = null;
        switch (current) {
            case PENDING:    next = OrderStatus.RECEIVED;  break;
            case RECEIVED:   next = OrderStatus.PREPARING; break;
            case PREPARING:  next = OrderStatus.READY;     break;
            case READY:      next = OrderStatus.SERVED;    break;
            default: break;
        }
        if (next != null) {
            orderRepository.updateStatus(orderId, next);
        }
    }

    public synchronized void setOrderStatus(String orderId, OrderStatus status) {
        orderRepository.updateStatus(orderId, status);
    }

    public int getPendingOrderCount() {
        List<Order> all = orderRepository.getAll();
        int count = 0;
        for (Order o : all) {
            OrderStatus s = o.getStatus();
            if (s == OrderStatus.PENDING || s == OrderStatus.RECEIVED || s == OrderStatus.PREPARING) {
                count++;
            }
        }
        return count;
    }
}
