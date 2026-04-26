package ui;

import exception.OrderException;
import model.MenuItem;
import model.Order;
import repository.MenuRepository;
import service.OrderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class OrderPanel extends JPanel {
    private final OrderService orderService;
    private final MenuRepository menuRepository;
    private JTextField tableField, customerField;
    private JTable menuSelectTable, cartTable, orderStatusTable;
    private DefaultTableModel menuSelectModel, cartModel, orderStatusModel;
    private JSpinner qtySpinner;
    private JLabel cartTotalLabel;
    private java.util.Timer refreshTimer;

    // Cart: maps itemId -> {MenuItem, quantity}
    private final Map<String, CartEntry> cart = new LinkedHashMap<>();

    private static final Color BG = new Color(25, 25, 35), CARD = new Color(32, 32, 48);
    private static final Color ACCENT = new Color(60, 200, 220), FG = new Color(210, 210, 225);

    public OrderPanel(OrderService orderService, MenuRepository menuRepository) {
        this.orderService   = orderService;
        this.menuRepository = menuRepository;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(BG);

        // Title
        JLabel title = new JLabel("Order Management", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(ACCENT);
        add(title, BorderLayout.NORTH);

        // Center: new-order form (menu + cart) | live status
        JPanel center = new JPanel(new GridLayout(1, 2, 12, 0));
        center.setBackground(BG);
        center.add(buildNewOrder());
        center.add(buildStatus());
        add(center, BorderLayout.CENTER);

        // Buttons
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnP.setBackground(BG);
        JButton placeBtn = btn("Place Order", new Color(55, 170, 95));
        JButton serveBtn = btn("Mark Served", new Color(220, 145, 40));
        JButton cancelBtn= btn("Cancel Order",new Color(190, 65, 65));
        JButton refBtn   = btn("Refresh",     new Color(80, 90, 160));
        placeBtn.addActionListener(e  -> placeOrder());
        serveBtn.addActionListener(e  -> markServed());
        cancelBtn.addActionListener(e -> cancelOrder());
        refBtn.addActionListener(e    -> refreshOrderStatus());
        btnP.add(placeBtn); btnP.add(serveBtn); btnP.add(cancelBtn); btnP.add(refBtn);
        add(btnP, BorderLayout.SOUTH);

        startAutoRefresh();
    }

    private JPanel buildNewOrder() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ACCENT),
                        " New Order ", 0, 0, new Font("SansSerif", Font.BOLD, 12), ACCENT),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        // Table no + customer
        JPanel top = new JPanel(new GridLayout(2, 2, 6, 4));
        top.setBackground(CARD);
        tableField = field(); customerField = field();
        top.add(lbl("Table No:")); top.add(tableField);
        top.add(lbl("Customer:")); top.add(customerField);
        p.add(top, BorderLayout.NORTH);

        // Split: menu items (top) + cart (bottom)
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setBackground(CARD);
        split.setDividerSize(5);
        split.setResizeWeight(0.55);

        // --- Menu items table ---
        JPanel menuPanel = new JPanel(new BorderLayout(4, 4));
        menuPanel.setBackground(CARD);

        String[] menuCols = {"ID", "Name", "Type", "Price"};
        menuSelectModel = new DefaultTableModel(menuCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        menuSelectTable = new JTable(menuSelectModel);
        styleTable(menuSelectTable);
        menuSelectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane menuSp = new JScrollPane(menuSelectTable);
        menuSp.getViewport().setBackground(new Color(38, 38, 52));
        menuPanel.add(menuSp, BorderLayout.CENTER);

        // Qty spinner + Add/Remove buttons
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        addPanel.setBackground(CARD);
        addPanel.add(lbl("Qty:"));
        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        qtySpinner.setPreferredSize(new Dimension(55, 24));
        qtySpinner.getEditor().getComponent(0).setBackground(new Color(45, 45, 60));
        qtySpinner.getEditor().getComponent(0).setForeground(Color.WHITE);
        addPanel.add(qtySpinner);
        JButton addBtn = btn("Add to Cart", new Color(55, 170, 95));
        addBtn.addActionListener(e -> addToCart());
        addPanel.add(addBtn);
        JButton removeBtn = btn("Remove", new Color(190, 65, 65));
        removeBtn.addActionListener(e -> removeFromCart());
        addPanel.add(removeBtn);
        JButton clearBtn = btn("Clear Cart", new Color(120, 120, 140));
        clearBtn.addActionListener(e -> clearCart());
        addPanel.add(clearBtn);
        menuPanel.add(addPanel, BorderLayout.SOUTH);

        split.setTopComponent(menuPanel);

        // --- Cart table ---
        JPanel cartPanel = new JPanel(new BorderLayout(4, 4));
        cartPanel.setBackground(CARD);

        JLabel cartLabel = lbl("  Cart Items:");
        cartLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        cartLabel.setForeground(ACCENT);
        cartPanel.add(cartLabel, BorderLayout.NORTH);

        String[] cartCols = {"Item", "Qty", "Unit Price", "Subtotal"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        styleTable(cartTable);
        JScrollPane cartSp = new JScrollPane(cartTable);
        cartSp.getViewport().setBackground(new Color(38, 38, 52));
        cartPanel.add(cartSp, BorderLayout.CENTER);

        cartTotalLabel = lbl("  Cart Total: Rs. 0.00");
        cartTotalLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        cartTotalLabel.setForeground(new Color(100, 230, 130));
        cartPanel.add(cartTotalLabel, BorderLayout.SOUTH);

        split.setBottomComponent(cartPanel);

        p.add(split, BorderLayout.CENTER);

        refreshMenuSelection();
        return p;
    }

    private JPanel buildStatus() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ACCENT),
                        " Live Order Status ", 0, 0, new Font("SansSerif", Font.BOLD, 12), ACCENT),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        String[] cols = {"Order ID", "Table", "Customer", "Status", "Total", "Time"};
        orderStatusModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        orderStatusTable = new JTable(orderStatusModel);
        styleTable(orderStatusTable);
        JScrollPane sp = new JScrollPane(orderStatusTable);
        sp.getViewport().setBackground(new Color(38, 38, 52));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ---- Cart operations ----

    private void addToCart() {
        int row = menuSelectTable.getSelectedRow();
        if (row < 0) { err("Select an item from the menu first."); return; }
        String itemId = (String) menuSelectModel.getValueAt(row, 0);
        int qty = (int) qtySpinner.getValue();

        MenuItem menuItem = findMenuItem(itemId);
        if (menuItem == null) { err("Item not found."); return; }

        CartEntry entry = cart.get(itemId);
        if (entry != null) {
            entry.quantity += qty;
        } else {
            cart.put(itemId, new CartEntry(menuItem, qty));
        }
        refreshCart();
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { err("Select an item from the cart to remove."); return; }
        // Find the entry by row index
        String itemId = new ArrayList<>(cart.keySet()).get(row);
        cart.remove(itemId);
        refreshCart();
    }

    private void clearCart() {
        cart.clear();
        refreshCart();
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        double total = 0;
        for (CartEntry entry : cart.values()) {
            double unitPrice = entry.item.getTaxedPrice();
            double sub = unitPrice * entry.quantity;
            total += sub;
            cartModel.addRow(new Object[]{
                    entry.item.getName(),
                    entry.quantity,
                    String.format("%.2f", unitPrice),
                    String.format("%.2f", sub)
            });
        }
        cartTotalLabel.setText(String.format("  Cart Total: Rs. %.2f", total));
    }

    private MenuItem findMenuItem(String itemId) {
        for (MenuItem item : menuRepository.getAll())
            if (item.getItemId().equals(itemId)) return item;
        return null;
    }

    // ---- Order operations ----

    private void placeOrder() {
        try {
            String tableStr = tableField.getText().trim();
            String customer = customerField.getText().trim();
            if (tableStr.isEmpty()) { err("Enter table number."); return; }
            int tableNo = Integer.parseInt(tableStr);
            if (cart.isEmpty()) { err("Add at least one item to the cart."); return; }

            // Build item list, duplicating items per quantity
            List<MenuItem> items = new ArrayList<>();
            for (CartEntry entry : cart.values())
                for (int i = 0; i < entry.quantity; i++)
                    items.add(entry.item);

            Order order = orderService.placeOrder(tableNo, customer, items);
            JOptionPane.showMessageDialog(this, "Order placed! ID: " + order.getOrderId()
                            + "\nItems: " + cart.size() + " unique, " + items.size() + " total",
                    "Order Placed", JOptionPane.INFORMATION_MESSAGE);
            tableField.setText(""); customerField.setText("");
            cart.clear(); refreshCart();
            refreshOrderStatus();
        } catch (NumberFormatException ex) { err("Table must be a number.");
        } catch (OrderException ex) { err(ex.getMessage()); }
    }

    private void markServed() {
        int row = orderStatusTable.getSelectedRow();
        if (row < 0) { err("Select an order."); return; }
        try { orderService.markAsServed((String) orderStatusModel.getValueAt(row, 0)); refreshOrderStatus();
        } catch (OrderException ex) { err(ex.getMessage()); }
    }

    private void cancelOrder() {
        int row = orderStatusTable.getSelectedRow();
        if (row < 0) { err("Select an order."); return; }
        String oid = (String) orderStatusModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Cancel " + oid + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { orderService.cancelOrder(oid); refreshOrderStatus();
        } catch (OrderException ex) { err(ex.getMessage()); }
    }

    public void refreshMenuSelection() {
        menuSelectModel.setRowCount(0);
        for (MenuItem item : menuRepository.getAll())
            menuSelectModel.addRow(new Object[]{item.getItemId(), item.getName(),
                    item.getItemType(), String.format("%.2f", item.getTaxedPrice())});
    }

    public void refreshOrderStatus() {
        SwingUtilities.invokeLater(() -> {
            orderStatusModel.setRowCount(0);
            List<Order> orders = orderService.getAllOrders();
            orders.sort((a, b) -> b.getOrderId().compareTo(a.getOrderId()));
            for (Order o : orders)
                orderStatusModel.addRow(new Object[]{o.getOrderId(), o.getTableNumber(),
                        o.getCustomerName(), o.getStatus().toString(),
                        String.format("%.2f", o.getOrderTotal()), o.getOrderTime()});
        });
    }

    private void startAutoRefresh() {
        refreshTimer = new java.util.Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { refreshOrderStatus(); }
        }, 3000, 3000);
    }

    // Helpers
    private void err(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }
    private JLabel lbl(String t) { JLabel l = new JLabel(t); l.setForeground(FG); l.setFont(new Font("SansSerif", Font.PLAIN, 12)); return l; }
    private JTextField field() { JTextField f = new JTextField(); f.setBackground(new Color(45,45,60)); f.setForeground(Color.WHITE); f.setCaretColor(Color.WHITE); f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(70,70,100)), BorderFactory.createEmptyBorder(2,5,2,5))); return f; }
    private JButton btn(String t, Color bg) { JButton b = new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorderPainted(false); b.setFont(new Font("SansSerif", Font.BOLD, 11)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b; }
    private void styleTable(JTable t) { t.setBackground(new Color(38,38,52)); t.setForeground(FG); t.setGridColor(new Color(55,55,72)); t.getTableHeader().setBackground(new Color(45,45,62)); t.getTableHeader().setForeground(ACCENT); t.setRowHeight(24); t.setSelectionBackground(new Color(70,70,110)); }

    // Inner class for cart entries
    private static class CartEntry {
        MenuItem item;
        int quantity;
        CartEntry(MenuItem item, int quantity) { this.item = item; this.quantity = quantity; }
    }
}
