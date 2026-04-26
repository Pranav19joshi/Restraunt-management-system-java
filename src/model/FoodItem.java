package model;

public class FoodItem extends MenuItem {
    public FoodItem(String itemId, String name, String category, double basePrice) {
        super(itemId, name, category, basePrice);
    }

    @Override
    public String getItemType() {
        return "Food";
    }
}
