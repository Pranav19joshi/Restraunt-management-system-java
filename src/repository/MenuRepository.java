package repository;

import interfaces.Manageable;
import model.DrinkItem;
import model.FoodItem;
import model.MenuItem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MenuRepository implements Manageable<MenuItem> {
    private static final String FILE_PATH = "data/menu.csv";
    private List<MenuItem> menuItems = new ArrayList<>();

    public MenuRepository() {
        loadFromFile();
        if (menuItems.isEmpty()) { seedDefaults(); saveToFile(); }
    }

    private void seedDefaults() {
        menuItems.add(new FoodItem("F001", "Butter Chicken",   "Main Course", 280.0));
        menuItems.add(new FoodItem("F002", "Paneer Tikka",     "Starters",    180.0));
        menuItems.add(new FoodItem("F003", "Dal Makhani",      "Main Course", 160.0));
        menuItems.add(new FoodItem("F004", "Chicken Biryani",  "Rice",        250.0));
        menuItems.add(new FoodItem("F005", "Veg Biryani",      "Rice",        200.0));
        menuItems.add(new FoodItem("F006", "Garlic Naan",      "Breads",       50.0));
        menuItems.add(new FoodItem("F007", "Samosa",           "Starters",     40.0));
        menuItems.add(new FoodItem("F008", "Gulab Jamun",      "Desserts",     80.0));
        menuItems.add(new DrinkItem("D001", "Mango Lassi",     "Beverages",    90.0));
        menuItems.add(new DrinkItem("D002", "Masala Chai",     "Beverages",    40.0));
        menuItems.add(new DrinkItem("D003", "Cold Coffee",     "Beverages",   110.0));
        menuItems.add(new DrinkItem("D004", "Fresh Lime Soda", "Beverages",    60.0));
    }

    @Override public void add(MenuItem item) { menuItems.add(item); saveToFile(); }
    @Override public void remove(String id) { menuItems.removeIf(i -> i.getItemId().equals(id)); saveToFile(); }
    @Override public MenuItem findById(String id) { return menuItems.stream().filter(i -> i.getItemId().equals(id)).findFirst().orElse(null); }
    @Override public List<MenuItem> getAll() { return new ArrayList<>(menuItems); }

    public void loadFromFile() {
        new File("data").mkdirs();
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length < 5) continue;
                String id = p[0].trim(), name = p[1].trim();
                double price = Double.parseDouble(p[2].trim());
                String cat = p[3].trim(), type = p[4].trim();
                if ("Food".equalsIgnoreCase(type))
                    menuItems.add(new FoodItem(id, name, cat, price));
                else
                    menuItems.add(new DrinkItem(id, name, cat, price));
            }
        } catch (IOException | NumberFormatException e) { System.err.println("Error loading menu: " + e.getMessage()); }
    }

    public void saveToFile() {
        new File("data").mkdirs();
        try (BufferedWriter w = new BufferedWriter(new FileWriter(FILE_PATH))) {
            w.write("itemId,name,basePrice,category,itemType"); w.newLine();
            for (MenuItem item : menuItems) {
                w.write(String.format("%s,%s,%.1f,%s,%s", item.getItemId(), item.getName(),
                        item.getBasePrice(), item.getCategory(), item.getItemType()));
                w.newLine();
            }
        } catch (IOException e) { System.err.println("Error saving menu: " + e.getMessage()); }
    }
}
