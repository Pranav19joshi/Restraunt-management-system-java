package model;

public class DrinkItem extends MenuItem {
    public static final double TAX_RATE = 0.12;
    private boolean isAlcoholic;
    private String volume;

    public DrinkItem(String itemId, String name, String category, double basePrice, boolean available,
                     boolean isAlcoholic, String volume) {
        super(itemId, name, category, basePrice, available);
        this.isAlcoholic = isAlcoholic;
        this.volume = volume;
    }

    @Override
    public double getTaxedPrice() {
        return getBasePrice() * (1 + TAX_RATE);
    }

    @Override
    public String getItemType() {
        return "Drink";
    }

    public boolean isAlcoholic() { return isAlcoholic; }
    public void setAlcoholic(boolean alcoholic) { isAlcoholic = alcoholic; }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
}
