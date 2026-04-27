package model;

import enums.OrderStatus;
import interfaces.Orderable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Order implements Orderable {
    private String orderId, customerName, assignedWaiter, orderTime;
    private OrderStatus status;
    private List<MenuItem> items;

    public Order(String orderId, String customerName, List<MenuItem> items) {
        this.orderId = orderId;
        this.customerName = customerName; this.items = new ArrayList<>(items);
        this.status = OrderStatus.PENDING; this.assignedWaiter = "";
        this.orderTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
    }

    @Override public String getOrderId() { return orderId; }
    @Override public OrderStatus getStatus() { return status; }
    @Override public void setStatus(OrderStatus status) { this.status = status; }
    @Override public double getOrderTotal() { return Math.round(items.stream().mapToDouble(MenuItem::getTaxedPrice).sum() * 100.0) / 100.0; }
    @Override public List<String> getItemNames() { return items.stream().map(MenuItem::getName).collect(Collectors.toList()); }


    public String getCustomerName() { return customerName; }
    public void setCustomerName(String n) { this.customerName = n; }
    public String getAssignedWaiter() { return assignedWaiter; }
    public void setAssignedWaiter(String w) { this.assignedWaiter = w; }
    public String getOrderTime() { return orderTime; }
    public void setOrderTime(String t) { this.orderTime = t; }
    public List<MenuItem> getItems() { return items; }

    public String toCsvString() {
        String itemNames = getItemNames().stream().collect(Collectors.joining("|"));
        return String.format("%s,%s,%s,%.2f,%s,%s",
                orderId, customerName, status.name(), getOrderTotal(), orderTime, itemNames);
    }

    @Override
    public String toString() { return orderId + " | " + customerName + " | " + status; }
}
