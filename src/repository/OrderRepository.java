package repository;

import enums.OrderStatus;
import model.MenuItem;
import model.Order;
import model.FoodItem;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class OrderRepository {
    private final MenuRepository menuRepository;
    private Map<String, Order> orders = new HashMap<>();

    public OrderRepository(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
        loadFromDatabase();
    }

    public void save(Order order) { 
        orders.put(order.getOrderId(), order); 
        saveToDatabase(order); 
    }

    public Order findById(String orderId) { 
        return orders.get(orderId); 
    }
    
    public List<Order> getAll() { 
        return new ArrayList<>(orders.values()); 
    }

    public int getMaxOrderCounter() {
        int max = 0;
        for (String id : orders.keySet()) {
            try {
                int val = Integer.parseInt(id.replace("ORD-", ""));
                if (val > max) max = val;
            } catch (Exception e) {}
        }
        return max;
    }

    public void updateStatus(String orderId, OrderStatus status) {
        Order order = orders.get(orderId);
        if (order != null) { 
            order.setStatus(status); 
            updateStatusInDatabase(orderId, status); 
        }
    }

    public List<Order> getByStatus(OrderStatus status) {
        return orders.values().stream().filter(o -> o.getStatus() == status).collect(Collectors.toList());
    }

    public void loadFromDatabase() {
        orders.clear();
        String query = "SELECT * FROM orders";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
             
            while (rs.next()) {
                String orderId = rs.getString("orderId");
                String custName = rs.getString("customerName");
                String statusStr = rs.getString("status");
                String orderTime = rs.getString("orderTime");
                
                // Get items
                List<MenuItem> items = new ArrayList<>();
                try (PreparedStatement itemStmt = conn.prepareStatement("SELECT itemName FROM order_items WHERE orderId = ?")) {
                    itemStmt.setString(1, orderId);
                    try (ResultSet itemRs = itemStmt.executeQuery()) {
                        while (itemRs.next()) {
                            String itemName = itemRs.getString("itemName");
                            MenuItem actualItem = menuRepository.getAll().stream()
                                    .filter(m -> m.getName().equals(itemName))
                                    .findFirst()
                                    .orElse(new FoodItem("?", itemName, "Unknown", 0.0, false, 0));
                            items.add(actualItem);
                        }
                    }
                }
                
                Order order = new Order(orderId, custName, items);
                order.setOrderTime(orderTime);
                try { 
                    order.setStatus(OrderStatus.valueOf(statusStr)); 
                } catch (IllegalArgumentException ex) { 
                    order.setStatus(OrderStatus.PENDING); 
                }
                orders.put(orderId, order);
            }
        } catch (SQLException e) { 
            System.err.println("Error loading orders from DB: " + e.getMessage()); 
        }
    }

    private void saveToDatabase(Order order) {
        String query = "INSERT INTO orders (orderId, customerName, status, total, orderTime) VALUES (?, ?, ?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE status=?, total=?, orderTime=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setString(1, order.getOrderId());
            pstmt.setString(2, order.getCustomerName());
            pstmt.setString(3, order.getStatus().name());
            pstmt.setDouble(4, order.getOrderTotal());
            pstmt.setString(5, order.getOrderTime());
            
            pstmt.setString(6, order.getStatus().name());
            pstmt.setDouble(7, order.getOrderTotal());
            pstmt.setString(8, order.getOrderTime());
            
            pstmt.executeUpdate();
            
            // Delete old items and insert new ones
            try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM order_items WHERE orderId = ?")) {
                delStmt.setString(1, order.getOrderId());
                delStmt.executeUpdate();
            }
            
            String itemQuery = "INSERT INTO order_items (orderId, itemName) VALUES (?, ?)";
            try (PreparedStatement itemStmt = conn.prepareStatement(itemQuery)) {
                for (MenuItem item : order.getItems()) {
                    itemStmt.setString(1, order.getOrderId());
                    itemStmt.setString(2, item.getName());
                    itemStmt.executeUpdate();
                }
            }
            
        } catch (SQLException e) { 
            System.err.println("Error saving order to DB: " + e.getMessage()); 
        }
    }

    public void updateStatusInDatabase(String orderId, OrderStatus newStatus) {
        String query = "UPDATE orders SET status = ? WHERE orderId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newStatus.name());
            pstmt.setString(2, orderId);
            pstmt.executeUpdate();
        } catch (SQLException e) { 
            System.err.println("Error updating order status in DB: " + e.getMessage()); 
        }
    }
}
