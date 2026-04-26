package ui;

import concurrent.KitchenWorkerThread;
import concurrent.NotificationThread;
import repository.MenuRepository;
import repository.OrderRepository;
import service.BillingService;
import service.KitchenService;
import service.OrderService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JLabel notificationBar;
    private javax.swing.Timer notifResetTimer;

    public MainFrame() {
        setTitle("Restaurant Management System");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(20, 20, 30));

        // Wire up dependencies
        MenuRepository menuRepo   = new MenuRepository();
        OrderRepository orderRepo = new OrderRepository(menuRepo);
        KitchenService kitchenSvc = new KitchenService(orderRepo);
        OrderService orderSvc     = new OrderService(orderRepo);
        BillingService billingSvc = new BillingService(orderRepo);

        // Background threads
        NotificationThread notifThread = new NotificationThread();
        notifThread.setListener((message, isReady) ->
                SwingUtilities.invokeLater(() -> showNotification(message, isReady)));
        KitchenWorkerThread kitchenWorker = new KitchenWorkerThread(kitchenSvc, notifThread);
        new Thread(kitchenWorker, "KitchenWorkerThread") {{ setDaemon(true); start(); }};
        new Thread(notifThread, "NotificationThread")    {{ setDaemon(true); start(); }};

        // Build UI
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(18, 18, 28));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JLabel titleLbl = new JLabel("Restaurant Management System");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setForeground(new Color(255, 200, 60));
        JLabel subLbl = new JLabel("OOP Project | Semester 4 | Java + Swing");
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLbl.setForeground(new Color(140, 140, 160));
        header.add(titleLbl, BorderLayout.WEST);
        header.add(subLbl, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(28, 28, 40));
        tabs.setForeground(Color.WHITE);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("Menu",      new MenuPanel(menuRepo));
        tabs.addTab("Orders",    new OrderPanel(orderSvc, menuRepo));
        tabs.addTab("Billing",   new BillingPanel(billingSvc, orderSvc));
        tabs.addTab("History",   new OrderHistoryPanel(orderRepo));
        add(tabs, BorderLayout.CENTER);

        // Notification bar
        JPanel notifPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        notifPanel.setBackground(new Color(18, 18, 28));
        notifPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(55, 55, 75)));
        notificationBar = new JLabel("  System ready. Waiting for orders...");
        notificationBar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        notificationBar.setForeground(new Color(110, 110, 135));
        notifPanel.add(notificationBar);
        add(notifPanel, BorderLayout.SOUTH);

        notifResetTimer = new javax.swing.Timer(5000, e -> {
            notificationBar.setText("  System ready. Waiting for orders...");
            notificationBar.setForeground(new Color(110, 110, 135));
        });
        notifResetTimer.setRepeats(false);

        setVisible(true);
    }

    public void showNotification(String message, boolean isReady) {
        notificationBar.setText("  " + message);
        notificationBar.setForeground(isReady ? new Color(60, 210, 100) : new Color(210, 80, 80));
        notifResetTimer.restart();
    }
}
