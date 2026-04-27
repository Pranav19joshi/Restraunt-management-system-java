package ui;

import enums.OrderStatus;
import model.Order;
import repository.OrderRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderHistoryPanel — Unit IV: Event Handling, KeyListener, ActionListener
 *                     Unit III: reads orders via OrderRepository.loadFromFile()
 */
public class OrderHistoryPanel extends JPanel {
    private final OrderRepository orderRepo;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private List<Order> allOrders;
    private JLabel revenueLabel, orderCountLabel;

    private static final Color BG = new Color(25, 25, 35), CARD = new Color(32, 32, 48);
    private static final Color ACCENT = new Color(99, 179, 237), FG = new Color(210, 210, 225);

    public OrderHistoryPanel(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildSummary(), BorderLayout.SOUTH);
        loadOrders();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(new Color(20, 20, 30));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 72)));

        bar.add(lbl("Search:"));
        searchField = new JTextField(18);
        searchField.setBackground(new Color(40, 40, 58)); searchField.setForeground(FG);
        searchField.setCaretColor(ACCENT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 100)),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        bar.add(searchField);

        bar.add(lbl("Status:"));
        statusFilter = new JComboBox<>(new String[]{"All", "PENDING", "RECEIVED", "PREPARING", "READY", "SERVED", "CANCELLED"});
        statusFilter.setBackground(new Color(40, 40, 58)); statusFilter.setForeground(FG);
        bar.add(statusFilter);

        JButton refBtn = new JButton("Refresh");
        refBtn.setBackground(new Color(55, 130, 195)); refBtn.setForeground(Color.WHITE);
        refBtn.setFont(new Font("SansSerif", Font.BOLD, 12)); refBtn.setFocusPainted(false);
        refBtn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        refBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refBtn.addActionListener(e -> loadOrders());
        bar.add(refBtn);

        // Unit IV – KeyListener for live search
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applyFilter(); }
        });
        // Unit IV – ActionListener for dropdown
        statusFilter.addActionListener(e -> applyFilter());
        return bar;
    }

    private JScrollPane buildTable() {
        String[] cols = {"Order ID", "Customer", "Status", "Total (Rs.)", "Time"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setBackground(CARD); table.setForeground(FG);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(26); table.setShowGrid(false);
        table.getTableHeader().setBackground(new Color(40, 40, 60));
        table.getTableHeader().setForeground(ACCENT);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setSelectionBackground(new Color(60, 80, 120));

        // Unit IV – mouse listener for row-click detail popup
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) showDetail(); }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(CARD);
        sp.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return sp;
    }

    private JPanel buildSummary() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        bar.setBackground(new Color(20, 20, 30));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 72)));

        orderCountLabel = new JLabel("Total Orders: 0");
        orderCountLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        orderCountLabel.setForeground(new Color(154, 117, 235));
        bar.add(orderCountLabel);

        revenueLabel = new JLabel("Total Revenue: Rs. 0.00");
        revenueLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        revenueLabel.setForeground(new Color(72, 207, 173));
        bar.add(revenueLabel);

        return bar;
    }

    private void loadOrders() {
        orderRepo.loadFromDatabase();
        allOrders = orderRepo.getAll();
        applyFilter();
    }

    private void applyFilter() {
        String query  = searchField.getText().toLowerCase().trim();
        String chosen = (String) statusFilter.getSelectedItem();
        tableModel.setRowCount(0);

        // Unit IV – stream filter with lambda
        List<Order> filtered = allOrders.stream().filter(o -> {
            boolean matchSearch = query.isEmpty()
                    || o.getCustomerName().toLowerCase().contains(query)
                    || o.getOrderId().toLowerCase().contains(query);
            boolean matchStatus = "All".equals(chosen) || o.getStatus().name().equals(chosen);
            return matchSearch && matchStatus;
        }).collect(Collectors.toList());

        double totalRevenue = 0;
        for (Order o : filtered) {
            totalRevenue += o.getOrderTotal();
            tableModel.addRow(new Object[]{o.getOrderId(), 
                    o.getCustomerName(), o.getStatus().getDisplayName(),
                    String.format("%.2f", o.getOrderTotal()), o.getOrderTime()});
        }

        orderCountLabel.setText("Total Orders: " + filtered.size());
        revenueLabel.setText(String.format("Total Revenue: Rs. %.2f", totalRevenue));
    }

    private void showDetail() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String orderId = (String) tableModel.getValueAt(row, 0);
        Order found = allOrders.stream().filter(o -> o.getOrderId().equals(orderId)).findFirst().orElse(null);
        if (found == null) return;
        String items = found.getItemNames().isEmpty() ? "(none)" : String.join(", ", found.getItemNames());
        String detail = String.format("Order: %s\nCustomer: %s\nStatus: %s\nTotal: Rs. %.2f\nTime: %s\nItems: %s",
                found.getOrderId(), found.getCustomerName(),
                found.getStatus().getDisplayName(), found.getOrderTotal(), found.getOrderTime(), items);
        JTextArea ta = new JTextArea(detail);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setEditable(false); ta.setBackground(CARD); ta.setForeground(FG);
        JOptionPane.showMessageDialog(this, ta, "Order Detail — " + orderId, JOptionPane.PLAIN_MESSAGE);
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setForeground(FG); l.setFont(new Font("SansSerif", Font.BOLD, 12)); return l;
    }
}
