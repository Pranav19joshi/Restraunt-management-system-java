package model;

public class FoodItem extends MenuItem {
    public static final double TAX_RATE = 0.05;
    private boolean isVegetarian;
    private int prepTimeMinutes;

    public FoodItem(String itemId, String name, String category, double basePrice, boolean available,
                    boolean isVegetarian, int prepTimeMinutes) {
        super(itemId, name, category, basePrice, available);
        this.isVegetarian = isVegetarian;
        this.prepTimeMinutes = prepTimeMinutes;
    }

    @Override
    public double getTaxedPrice() {
        return getBasePrice() * (1 + TAX_RATE);
    }

    @Override
    public String getItemType() {
        return "Food";
    }

    public boolean isVegetarian() { return isVegetarian; }
    public void setVegetarian(boolean vegetarian) { isVegetarian = vegetarian; }

    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public void setPrepTimeMinutes(int prepTimeMinutes) { this.prepTimeMinutes = prepTimeMinutes; }
}
