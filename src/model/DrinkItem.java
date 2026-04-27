package model;

public class DrinkItem extends MenuItem {

    private boolean isAlcoholic;

    public DrinkItem(String itemId, String name, String category, double basePrice,
                     boolean isAlcoholic) {
        super(itemId, name, category, basePrice);
        this.isAlcoholic = isAlcoholic;
    }



    @Override
    public String getItemType() {
        return "Drink";
    }

    public boolean isAlcoholic() { return isAlcoholic; }
    public void setAlcoholic(boolean alcoholic) { isAlcoholic = alcoholic; }


}
