package repository;

import interfaces.Manageable;
import model.DrinkItem;
import model.FoodItem;
import model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuRepository implements Manageable<MenuItem> {
    private List<MenuItem> menuItems = new ArrayList<>();

    public MenuRepository() {
        loadFromDatabase();
        if (menuItems.isEmpty()) { 
            seedDefaults(); 
        }
    }

    private void seedDefaults() {
        add(new FoodItem("F001", "Butter Chicken",   "Main Course", 280.0, false, 20));
        add(new FoodItem("F002", "Paneer Tikka",     "Starters",    180.0, true,  15));
        add(new FoodItem("F003", "Dal Makhani",      "Main Course", 160.0, true,  18));
        add(new FoodItem("F004", "Chicken Biryani",  "Rice",        250.0, false, 25));
        add(new FoodItem("F005", "Veg Biryani",      "Rice",        200.0, true,  22));
        add(new FoodItem("F006", "Garlic Naan",      "Breads",       50.0, true,   8));
        add(new FoodItem("F007", "Samosa",           "Starters",     40.0, true,  10));
        add(new FoodItem("F008", "Gulab Jamun",      "Desserts",     80.0, true,   5));
        add(new DrinkItem("D001", "Mango Lassi",     "Beverages",    90.0, false));
        add(new DrinkItem("D002", "Masala Chai",     "Beverages",    40.0, false));
        add(new DrinkItem("D003", "Cold Coffee",     "Beverages",   110.0, false));
        add(new DrinkItem("D004", "Fresh Lime Soda", "Beverages",    60.0, false));
    }

    @Override public void add(MenuItem item) { 
        menuItems.add(item); 
        saveToDatabase(item); 
    }
    
    @Override public void remove(String id) { 
        menuItems.removeIf(i -> i.getItemId().equals(id)); 
        removeFromDatabase(id); 
    }
    
    @Override public MenuItem findById(String id) { 
        return menuItems.stream().filter(i -> i.getItemId().equals(id)).findFirst().orElse(null); 
    }
    
    @Override public List<MenuItem> getAll() { 
        return new ArrayList<>(menuItems); 
    }

    public void loadFromDatabase() {
        menuItems.clear();
        String query = "SELECT * FROM menu_items";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
             
            while (rs.next()) {
                String id = rs.getString("itemId");
                String name = rs.getString("name");
                double price = rs.getDouble("basePrice");
                String cat = rs.getString("category");
                String type = rs.getString("itemType");
                String e1 = rs.getString("extra1");
                String e2 = rs.getString("extra2");
                
                if ("Food".equalsIgnoreCase(type)) {
                    menuItems.add(new FoodItem(id, name, cat, price, Boolean.parseBoolean(e1), Integer.parseInt(e2)));
                } else {
                    menuItems.add(new DrinkItem(id, name, cat, price, Boolean.parseBoolean(e1)));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading menu from DB: " + e.getMessage());
        }
    }

    public void saveToDatabase(MenuItem item) {
        String query = "INSERT INTO menu_items (itemId, name, basePrice, category, itemType, extra1, extra2) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE name=?, basePrice=?, category=?, itemType=?, extra1=?, extra2=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            String e1, e2;
            if (item instanceof FoodItem) { 
                FoodItem f = (FoodItem) item; 
                e1 = String.valueOf(f.isVegetarian()); 
                e2 = String.valueOf(f.getPrepTimeMinutes()); 
            } else { 
                DrinkItem d = (DrinkItem) item; 
                e1 = String.valueOf(d.isAlcoholic()); 
                e2 = "0"; 
            }
            
            pstmt.setString(1, item.getItemId());
            pstmt.setString(2, item.getName());
            pstmt.setDouble(3, item.getBasePrice());
            pstmt.setString(4, item.getCategory());
            pstmt.setString(5, item.getItemType());
            pstmt.setString(6, e1);
            pstmt.setString(7, e2);
            
            pstmt.setString(8, item.getName());
            pstmt.setDouble(9, item.getBasePrice());
            pstmt.setString(10, item.getCategory());
            pstmt.setString(11, item.getItemType());
            pstmt.setString(12, e1);
            pstmt.setString(13, e2);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving menu to DB: " + e.getMessage());
        }
    }

    public void removeFromDatabase(String id) {
        String query = "DELETE FROM menu_items WHERE itemId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting menu item from DB: " + e.getMessage());
        }
    }
}
