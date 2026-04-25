package model;

public abstract class MenuItem {
    private String itemId;
    private String name;
    private String category;
    private double basePrice;
    private boolean available;

    public MenuItem(String itemId, String name, String category, double basePrice, boolean available) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.basePrice = basePrice;
        this.available = available;
    }

    public abstract double getTaxedPrice();
    public abstract String getItemType();

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return name + " (" + getItemType() + ") - Rs. " + String.format("%.2f", getTaxedPrice());
    }
}
