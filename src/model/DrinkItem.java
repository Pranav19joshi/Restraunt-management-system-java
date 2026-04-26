package model;

public class DrinkItem extends MenuItem {
    public DrinkItem(String itemId, String name, String category, double basePrice) {
        super(itemId, name, category, basePrice);
    }

    @Override
    public String getItemType() {
        return "Drink";
    }
}
