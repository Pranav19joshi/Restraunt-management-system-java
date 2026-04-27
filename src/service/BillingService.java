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
        if (order == null)
            throw new PaymentException("Order not found: " + orderId);
        for (Bill b : bills.values())
            if (b.getOrderId().equals(orderId))
                return b;
        double subtotal = Math.round(order.getItems().stream().mapToDouble(MenuItem::getBasePrice).sum() * 100.0)
                / 100.0;
        double taxAmount = Math.round(
                order.getItems().stream().mapToDouble(i -> i.getTaxedPrice() - i.getBasePrice()).sum() * 100.0) / 100.0;
        String billId = String.format("BILL-%04d", ++billCounter);
        Bill bill = new Bill(billId, orderId, subtotal, taxAmount);
        bills.put(billId, bill);
        return bill;
    }

    public void applyDiscount(String billId, double amount) throws PaymentException {
        Bill bill = bills.get(billId);
        if (bill == null)
            throw new PaymentException("Bill not found: " + billId);
        if (bill.isPaid())
            throw new PaymentException("Bill is already paid.");
        if (amount < 0 || amount > bill.getTotal())
            throw new PaymentException(String.format("Discount must be 0-%.2f", bill.getTotal()), amount);
        bill.setDiscount(amount);
    }

    public double processPayment(String billId, PaymentMethod method, double amountPaid) throws PaymentException {
        Bill bill = bills.get(billId);
        if (bill == null)
            throw new PaymentException("Bill not found: " + billId);
        if (bill.isPaid())
            throw new PaymentException("Bill is already paid.");
        if (amountPaid < bill.getTotal())
            throw new PaymentException(String.format("Insufficient payment. Required: %.2f", bill.getTotal()),
                    amountPaid);
        bill.setPaymentMethod(method);
        bill.markAsPaid();
        return Math.round((amountPaid - bill.getTotal()) * 100.0) / 100.0;
    }

    public Bill getBill(String billId) {
        return bills.get(billId);
    }

    public Bill getBillForOrder(String orderId) {
        for (Bill b : bills.values())
            if (b.getOrderId().equals(orderId))
                return b;
        return null;
    }
}
