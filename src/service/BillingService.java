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

    public BillingService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Bill generateBill(String orderId) throws PaymentException {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new PaymentException("Order not found: " + orderId);
        }
        // Check if bill already exists for this order
        for (Bill b : bills.values()) {
            if (b.getOrderId().equals(orderId)) {
                return b; // return existing bill
            }
        }

        double subtotal = order.getItems().stream()
                .mapToDouble(MenuItem::getBasePrice).sum();
        double taxAmount = order.getItems().stream()
                .mapToDouble(item -> item.getTaxedPrice() - item.getBasePrice()).sum();

        billCounter++;
        String billId = String.format("BILL-%04d", billCounter);
        Bill bill = new Bill(billId, orderId, subtotal, taxAmount);
        bills.put(billId, bill);
        return bill;
    }

    public void applyDiscount(String billId, double discountAmount) throws PaymentException {
        Bill bill = bills.get(billId);
        if (bill == null) {
            throw new PaymentException("Bill not found: " + billId);
        }
        if (bill.isPaid()) {
            throw new PaymentException("Bill is already paid.");
        }
        if (discountAmount < 0 || discountAmount > bill.getTotal()) {
            throw new PaymentException("Discount must be between 0 and " + bill.getTotal(), discountAmount);
        }
        bill.setDiscount(discountAmount);
    }

    public double processPayment(String billId, PaymentMethod paymentMethod, double amountPaid) throws PaymentException {
        Bill bill = bills.get(billId);
        if (bill == null) {
            throw new PaymentException("Bill not found: " + billId);
        }
        if (bill.isPaid()) {
            throw new PaymentException("Bill is already paid.");
        }
        double totalDue = bill.getTotal();
        if (amountPaid < totalDue) {
            throw new PaymentException("Insufficient payment. Required: " + totalDue, amountPaid);
        }
        bill.setPaymentMethod(paymentMethod);
        bill.markAsPaid();
        return amountPaid - totalDue; // change
    }

    public Bill getBill(String billId) {
        return bills.get(billId);
    }

    public Bill getBillForOrder(String orderId) {
        for (Bill b : bills.values()) {
            if (b.getOrderId().equals(orderId)) return b;
        }
        return null;
    }
}
