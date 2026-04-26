package model;

public class Customer extends Person {
    private int tableNumber;
    private String visitDate;

    public Customer(String id, String name, String phone, String email, int tableNumber, String visitDate) {
        super(id, name, phone, email);
        this.tableNumber = tableNumber;
        this.visitDate = visitDate;
    }

    @Override
    public String getRole() {
        return null;
    }

    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }
}
