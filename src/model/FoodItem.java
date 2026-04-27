package model;

public class FoodItem extends MenuItem {

    private boolean isVegetarian;
    private int prepTimeMinutes;

    public FoodItem(String itemId, String name, String category, double basePrice,
                    boolean isVegetarian, int prepTimeMinutes) {
        super(itemId, name, category, basePrice);
        this.isVegetarian = isVegetarian;
        this.prepTimeMinutes = prepTimeMinutes;
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
