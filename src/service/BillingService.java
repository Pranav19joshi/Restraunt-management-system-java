package service;

import enums.PaymentMethod;
import exception.PaymentException;
import model.Bill;
import model.MenuItem;
import model.Order;
import repository.OrderRepository;

import java.util.HashMap;
import java.util.Map;

public class BillingService {
    private final OrderRepository orderRepository;
    private final Map<String, Bill> bills = new HashMap<>();
    private int billCounter = 0;

    public BillingService(OrderRepository orderRepository) { this.orderRepository = orderRepository; }

    public Bill generateBill(String orderId) throws PaymentException {
        Order order = orderRepository.findById(orderId);
        if (order == null) throw new PaymentException("Order not found: " + orderId);
        for (Bill b : bills.values())
            if (b.getOrderId().equals(orderId)) return b;
        
        double subtotal  = Math.round(order.getItems().stream().mapToDouble(MenuItem::getBasePrice).sum() * 100.0) / 100.0;
        double taxedTotal = Math.round(order.getItems().stream().mapToDouble(MenuItem::getTaxedPrice).sum() * 100.0) / 100.0;
        double taxAmount = Math.round((taxedTotal - subtotal) * 100.0) / 100.0;
        
        String billId = String.format("BILL-%04d", ++billCounter);
        Bill bill = new Bill(billId, orderId, subtotal, taxAmount);
        bills.put(billId, bill);
        return bill;
    }

    public void applyDiscount(String billId, double amount) throws PaymentException {
        Bill bill = bills.get(billId);
        if (bill == null) throw new PaymentException("Bill not found: " + billId);
        if (bill.isPaid()) throw new PaymentException("Bill is already paid.");
        double roundedAmount = Math.round(amount * 100.0) / 100.0;
        if (roundedAmount < 0 || roundedAmount > bill.getTotal()) throw new PaymentException("Discount must be 0-" + bill.getTotal(), roundedAmount);
        bill.setDiscount(roundedAmount);
    }

    public double processPayment(String billId, PaymentMethod method, double amountPaid) throws PaymentException {
        Bill bill = bills.get(billId);
        if (bill == null) throw new PaymentException("Bill not found: " + billId);
        if (bill.isPaid()) throw new PaymentException("Bill is already paid.");
        
        double required = bill.getTotal();
        if (amountPaid < required - 0.01) { // 1 paisa tolerance
            throw new PaymentException("Insufficient payment. Required: Rs. " + String.format("%.2f", required), amountPaid);
        }
        
        bill.setPaymentMethod(method);
        bill.markAsPaid();
        return Math.round((amountPaid - required) * 100.0) / 100.0;
    }

    public Bill getBill(String billId) { return bills.get(billId); }

    public Bill getBillForOrder(String orderId) {
        for (Bill b : bills.values()) if (b.getOrderId().equals(orderId)) return b;
        return null;
    }
}
