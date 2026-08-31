package model;

import java.sql.Timestamp;

/**
 * Supplier subclass representing a supplier entity.
 * Table: suppliers
 */
public class Supplier extends User {

    private int supplierId;
    private String supplierName;
    private String address;
    private String contactNo;

    public Supplier() {
        setRole("SUPPLIER");
        setStatus("PENDING");
    }

    public Supplier(int userId, String username, String password, String phoneNo, String pin, String status, Timestamp createdAt,
                    int supplierId, String supplierName, String address, String contactNo) {
        super(userId, username, password, phoneNo, pin, "SUPPLIER", status, createdAt);
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.address = address;
        this.contactNo = contactNo;
    }

    public Supplier(String username, String password, String phoneNo, String pin,
                    String supplierName, String address, String contactNo) {
        super(username, password, phoneNo, pin, "SUPPLIER", "PENDING");
        this.supplierName = supplierName;
        this.address = address;
        this.contactNo = contactNo;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "supplierId=" + supplierId +
                ", userId=" + getUserId() +
                ", username='" + getUsername() + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", address='" + address + '\'' +
                ", contactNo='" + contactNo + '\'' +
                ", phoneNo='" + getPhoneNo() + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
    }
}
