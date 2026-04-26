package repository;

import enums.OrderStatus;
import model.MenuItem;
import model.Order;
import model.FoodItem;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class OrderRepository {
    private static final String FILE_PATH = "data/orders.csv";
    private final MenuRepository menuRepository;
    private Map<String, Order> orders = new HashMap<>();

    public OrderRepository(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
        new File("data").mkdirs();
        ensureHeaderExists();
        loadFromFile();
    }

    private void ensureHeaderExists() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
                w.write("orderId,tableNumber,customerName,status,total,orderTime,items");
                w.newLine();
            } catch (IOException e) { System.err.println("Error creating orders file: " + e.getMessage()); }
        }
    }

    public void save(Order order) { orders.put(order.getOrderId(), order); appendToCsv(order); }

    private void appendToCsv(Order order) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            w.write(order.toCsvString()); w.newLine();
        } catch (IOException e) { System.err.println("Error appending order: " + e.getMessage()); }
    }

    public Order findById(String orderId) { return orders.get(orderId); }
    public List<Order> getAll() { return new ArrayList<>(orders.values()); }

    public void updateStatus(String orderId, OrderStatus status) {
        Order order = orders.get(orderId);
        if (order != null) { order.setStatus(status); updateStatusInFile(orderId, status); }
    }

    public List<Order> getByStatus(OrderStatus status) {
        return orders.values().stream().filter(o -> o.getStatus() == status).collect(Collectors.toList());
    }

    // Unit III – FileReader: load all past orders from CSV into memory
    public void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        orders.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",", 7);
                if (p.length < 6) continue;
                String orderId = p[0].trim();
                int tableNo    = Integer.parseInt(p[1].trim());
                String custName = p[2].trim(), statusStr = p[3].trim();
                String orderTime = p[5].trim();
                String itemsStr  = p.length > 6 ? p[6].trim() : "";

                List<MenuItem> items = new ArrayList<>();
                if (!itemsStr.isEmpty()) {
                    for (String name : itemsStr.split("\\|")) {
                        String trimmedName = name.trim();
                        if (trimmedName.isEmpty()) continue;
                        
                        // Lookup the item in MenuRepository to get its real price
                        MenuItem menuItem = menuRepository.getAll().stream()
                            .filter(i -> i.getName().equalsIgnoreCase(trimmedName))
                            .findFirst()
                            .orElse(null);
                        
                        if (menuItem != null) {
                            items.add(menuItem);
                        } else {
                            // Fallback if item not found in menu
                            items.add(new FoodItem("?", trimmedName, "Unknown", 0.0));
                        }
                    }
                }

                Order order = new Order(orderId, tableNo, custName, items);
                order.setOrderTime(orderTime);
                try { order.setStatus(OrderStatus.valueOf(statusStr)); }
                catch (IllegalArgumentException ex) { order.setStatus(OrderStatus.PENDING); }
                orders.put(orderId, order);
            }
        } catch (IOException | NumberFormatException e) { System.err.println("Error loading orders: " + e.getMessage()); }
    }

    public void updateStatusInFile(String orderId, OrderStatus newStatus) {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine();
            if (header != null) lines.add(header);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", 7);
                if (p.length >= 4 && p[0].trim().equals(orderId)) {
                    p[3] = newStatus.name(); lines.add(String.join(",", p));
                } else lines.add(line);
            }
        } catch (IOException e) { System.err.println("Error reading orders: " + e.getMessage()); return; }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String l : lines) { bw.write(l); bw.newLine(); }
        } catch (IOException e) { System.err.println("Error writing orders: " + e.getMessage()); }
    }
}
