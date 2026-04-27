package service;

import concurrent.OrderQueueManager;
import enums.OrderStatus;
import exception.OrderException;
import model.MenuItem;
import model.Order;
import repository.OrderRepository;

import java.util.List;

public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderQueueManager queueManager;
    private int orderCounter = 0;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.queueManager = OrderQueueManager.getInstance();
        this.orderCounter = orderRepository.getMaxOrderCounter();
    }

    public Order placeOrder(String customerName, List<MenuItem> items) throws OrderException {
        if (customerName == null || customerName.trim().isEmpty()) throw new OrderException("Customer name cannot be empty.");
        if (items == null || items.isEmpty()) throw new OrderException("Cannot place an order with no items.");
        String orderId = String.format("ORD-%04d", ++orderCounter);
        Order order = new Order(orderId, customerName.trim(), items);
        orderRepository.save(order);
        queueManager.enqueue(order);
        return order;
    }

    public void cancelOrder(String orderId) throws OrderException {
        Order order = orderRepository.findById(orderId);
        if (order == null) throw new OrderException("Order not found: " + orderId, orderId);
        OrderStatus s = order.getStatus();
        if (s == OrderStatus.SERVED || s == OrderStatus.READY) throw new OrderException("Cannot cancel: " + s, orderId);
        if (s == OrderStatus.CANCELLED) throw new OrderException("Already cancelled.", orderId);
        orderRepository.updateStatus(orderId, OrderStatus.CANCELLED);
    }

    public void markAsServed(String orderId) throws OrderException {
        Order order = orderRepository.findById(orderId);
        if (order == null) throw new OrderException("Order not found: " + orderId, orderId);
        if (order.getStatus() != OrderStatus.READY) throw new OrderException("Must be READY first. Current: " + order.getStatus(), orderId);
        orderRepository.updateStatus(orderId, OrderStatus.SERVED);
    }

    public List<Order> getAllOrders() { return orderRepository.getAll(); }
    public Order getOrderById(String orderId) { return orderRepository.findById(orderId); }
}
