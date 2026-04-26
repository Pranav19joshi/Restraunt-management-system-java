package ui;

import enums.PaymentMethod;
import exception.PaymentException;
import model.Bill;
import model.Order;
import service.BillingService;
import service.OrderService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BillingPanel extends JPanel {
    private final BillingService billingService;
    private final OrderService orderService;
    private JComboBox<String> orderCombo;
    private JTextField discountField, amountPaidField;
    private JComboBox<PaymentMethod> paymentCombo;
    private JTextArea receiptArea;
    private JLabel billIdLabel, totalLabel;
    private String currentBillId = null;

    private static final Color BG = new Color(25, 25, 35), CARD = new Color(32, 32, 48);
    private static final Color ACCENT = new Color(255, 140, 40), FG = new Color(210, 210, 225);

    public BillingPanel(BillingService billingService, OrderService orderService) {
        this.billingService = billingService;
        this.orderService   = orderService;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(BG);

        // Title
        JLabel title = new JLabel("Billing & Payment", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(ACCENT);
        add(title, BorderLayout.NORTH);

        // Controls left, receipt right
        JPanel center = new JPanel(new GridLayout(1, 2, 12, 0));
        center.setBackground(BG);
        center.add(buildControls());
        center.add(buildReceipt());
        add(center, BorderLayout.CENTER);
    }

    private JPanel buildControls() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ACCENT),
                        " Billing Controls ", 0, 0, new Font("SansSerif", Font.BOLD, 12), ACCENT),
                BorderFactory.createEmptyBorder(8, 12, 10, 12)));

        // Order selection
        p.add(lbl("Select Order:"));
        orderCombo = new JComboBox<>();
        orderCombo.setBackground(new Color(45, 45, 60)); orderCombo.setForeground(Color.WHITE);
        loadOrders();
        p.add(full(orderCombo));
        p.add(Box.createVerticalStrut(4));
        JButton genBtn = btn("Generate Bill", ACCENT);
        genBtn.addActionListener(e -> generateBill());
        p.add(full(genBtn));

        // Bill info
        p.add(Box.createVerticalStrut(8));
        billIdLabel = lbl("Bill ID: ---");
        totalLabel  = lbl("Total: Rs. ---");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        totalLabel.setForeground(new Color(100, 230, 130));
        p.add(full(billIdLabel)); p.add(full(totalLabel));

        // Discount
        p.add(Box.createVerticalStrut(8));
        p.add(lbl("Discount (Rs):"));
        discountField = field("0");
        p.add(full(discountField));
        p.add(Box.createVerticalStrut(4));
        JButton discBtn = btn("Apply Discount", new Color(90, 155, 75));
        discBtn.addActionListener(e -> applyDiscount());
        p.add(full(discBtn));

        // Payment
        p.add(Box.createVerticalStrut(8));
        p.add(lbl("Payment Method:"));
        paymentCombo = new JComboBox<>(PaymentMethod.values());
        paymentCombo.setBackground(new Color(45, 45, 60)); paymentCombo.setForeground(Color.WHITE);
        p.add(full(paymentCombo));
        p.add(Box.createVerticalStrut(4));
        p.add(lbl("Amount Paid (Rs):"));
        amountPaidField = field("0");
        p.add(full(amountPaidField));
        p.add(Box.createVerticalStrut(4));
        JButton payBtn = btn("Process Payment", new Color(55, 170, 95));
        payBtn.addActionListener(e -> processPayment());
        p.add(full(payBtn));

        // Refresh
        p.add(Box.createVerticalStrut(8));
        JButton refBtn = btn("Refresh Orders", new Color(80, 90, 155));
        refBtn.addActionListener(e -> loadOrders());
        p.add(full(refBtn));
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildReceipt() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ACCENT),
                        " Receipt ", 0, 0, new Font("SansSerif", Font.BOLD, 12), ACCENT),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        receiptArea = new JTextArea("\n  Generate a bill to see the receipt here.");
        receiptArea.setFont(new Font("Courier New", Font.PLAIN, 13));
        receiptArea.setBackground(new Color(18, 18, 28));
        receiptArea.setForeground(new Color(160, 240, 160));
        receiptArea.setEditable(false);
        p.add(new JScrollPane(receiptArea), BorderLayout.CENTER);
        return p;
    }

    // Actions
    private void loadOrders() {
        orderCombo.removeAllItems();
        List<Order> orders = orderService.getAllOrders();
        orders.sort((a, b) -> b.getOrderId().compareTo(a.getOrderId()));
        for (Order o : orders)
            orderCombo.addItem(o.getOrderId() + " | T" + o.getTableNumber() + " | " + o.getCustomerName() + " [" + o.getStatus() + "]");
    }

    private String getSelectedOrderId() {
        String s = (String) orderCombo.getSelectedItem();
        return s == null ? null : s.split(" ")[0];
    }

    private void generateBill() {
        String oid = getSelectedOrderId();
        if (oid == null) { err("Select an order."); return; }
        try {
            Bill bill = billingService.generateBill(oid);
            currentBillId = bill.getBillId();
            billIdLabel.setText("Bill ID: " + bill.getBillId());
            totalLabel.setText(String.format("Total: Rs. %.2f", bill.getTotal()));
            receiptArea.setText(bill.generateReceiptText());
            amountPaidField.setText(String.format("%.2f", bill.getTotal()));
        } catch (PaymentException ex) { err(ex.getMessage()); }
    }

    private void applyDiscount() {
        if (currentBillId == null) { err("Generate a bill first."); return; }
        try {
            billingService.applyDiscount(currentBillId, Double.parseDouble(discountField.getText().trim()));
            Bill bill = billingService.getBill(currentBillId);
            totalLabel.setText(String.format("Total: Rs. %.2f", bill.getTotal()));
            receiptArea.setText(bill.generateReceiptText());
            amountPaidField.setText(String.format("%.2f", bill.getTotal()));
        } catch (NumberFormatException ex) { err("Enter a valid discount.");
        } catch (PaymentException ex) { err(ex.getMessage()); }
    }

    private void processPayment() {
        if (currentBillId == null) { err("Generate a bill first."); return; }
        try {
            double change = billingService.processPayment(currentBillId,
                    (PaymentMethod) paymentCombo.getSelectedItem(),
                    Double.parseDouble(amountPaidField.getText().trim()));
            receiptArea.setText(billingService.getBill(currentBillId).generateReceiptText());
            JOptionPane.showMessageDialog(this, String.format("Payment done! Change: Rs. %.2f", change),
                    "Payment Done", JOptionPane.INFORMATION_MESSAGE);
            currentBillId = null;
            billIdLabel.setText("Bill ID: ---"); totalLabel.setText("Total: Rs. ---");
        } catch (NumberFormatException ex) { err("Enter a valid amount.");
        } catch (PaymentException ex) { err(ex.getMessage()); }
    }

    // Helpers
    private void err(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }
    private JLabel lbl(String t) { JLabel l = new JLabel(t); l.setForeground(FG); l.setFont(new Font("SansSerif", Font.PLAIN, 12)); l.setAlignmentX(LEFT_ALIGNMENT); return l; }
    private JTextField field(String def) { JTextField f = new JTextField(def); f.setBackground(new Color(45,45,60)); f.setForeground(Color.WHITE); f.setCaretColor(Color.WHITE); f.setAlignmentX(LEFT_ALIGNMENT); f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(70,70,100)), BorderFactory.createEmptyBorder(2,5,2,5))); return f; }
    private JButton btn(String t, Color bg) { JButton b = new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorderPainted(false); b.setFont(new Font("SansSerif", Font.BOLD, 11)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setAlignmentX(LEFT_ALIGNMENT); return b; }
    private Component full(JComponent c) { c.setAlignmentX(LEFT_ALIGNMENT); c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + 4)); return c; }
}
