package model;

import enums.PaymentMethod;
import interfaces.Payable;

public class Bill implements Payable {
    private String billId, orderId;
    private double subtotal, taxAmount, discount, totalAmount;
    private PaymentMethod paymentMethod;
    private boolean paid;

    public Bill(String billId, String orderId, double subtotal, double taxAmount) {
        this.billId = billId; this.orderId = orderId;
        this.subtotal = subtotal; this.taxAmount = taxAmount;
        this.totalAmount = Math.round((subtotal + taxAmount) * 100.0) / 100.0; this.discount = 0.0; this.paid = false;
    }

    @Override public double getTotal() { return Math.round((totalAmount - discount) * 100.0) / 100.0; }
    @Override public boolean isPaid() { return paid; }
    @Override public void markAsPaid() { this.paid = true; }

    public String generateReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("         RESTAURANT RECEIPT\n");
        sb.append("========================================\n");
        sb.append("Bill ID   : ").append(billId).append("\n");
        sb.append("Order ID  : ").append(orderId).append("\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("Subtotal  : Rs. %8.2f%n", subtotal));
        sb.append(String.format("Tax       : Rs. %8.2f%n", taxAmount));
        if (discount > 0) sb.append(String.format("Discount  : Rs. %8.2f%n", discount));
        sb.append("----------------------------------------\n");
        sb.append(String.format("TOTAL     : Rs. %8.2f%n", getTotal()));
        sb.append("----------------------------------------\n");
        if (paid) { sb.append("Payment   : ").append(paymentMethod != null ? paymentMethod.getDisplayName() : "N/A").append("\n"); sb.append("Status    : PAID\n"); }
        else sb.append("Status    : UNPAID\n");
        sb.append("========================================\n");
        sb.append("  Thank you for dining with us!\n");
        sb.append("========================================\n");
        return sb.toString();
    }

    public String getBillId() { return billId; }
    public String getOrderId() { return orderId; }
    public double getSubtotal() { return subtotal; }
    public double getTaxAmount() { return taxAmount; }
    public double getTotalAmount() { return totalAmount; }
    public double getDiscount() { return discount; }
    public void setDiscount(double d) { this.discount = d; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod m) { this.paymentMethod = m; }
}
