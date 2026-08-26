package customer.model;

public class Customer {
    private int customerId;
    private int userId;
    private String customerName;
    private String address;

    public Customer() {
    }

    public Customer(int userId, String customerName, String address) {
        this.userId = userId;
        this.customerName = customerName;
        this.address = address;
    }

    public Customer(int customerId, int userId, String customerName, String address) {
        this.customerId = customerId;
        this.userId = userId;
        this.customerName = customerName;
        this.address = address;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    @Override
    public String toString() {
        return "Customer{"
                + "customerId=" + customerId
                + ", userId=" + userId
                + ", customerName='" + customerName + '\''
                + ", address='" + address + '\''
                + '}';
    }
}
