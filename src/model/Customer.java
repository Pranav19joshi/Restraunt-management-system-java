package model;

public class Customer extends Person {
    private String visitDate;

    public Customer(String id, String name, String phone, String email, String visitDate) {
        super(id, name, phone, email);
        this.visitDate = visitDate;
    }

    @Override
    public String getRole() {
        return null;
    }



    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }
}
