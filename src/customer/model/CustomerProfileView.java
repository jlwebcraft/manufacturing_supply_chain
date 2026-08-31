package customer.model;

public class CustomerProfileView {
    private int customerId;
    private String customerName;
    private String address;
    private String phoneNo;

    public CustomerProfileView() {
    }

    public CustomerProfileView(int customerId, String customerName, String address, String phoneNo) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.address = address;
        this.phoneNo = phoneNo;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
}
