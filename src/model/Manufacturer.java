package model;

import java.sql.Timestamp;

/**
 * Manufacturer subclass representing a manufacturing entity.
 * Table: manufacturers
 */
public class Manufacturer extends User {

    private int manufacturerId;
    private String manufacturerName;
    private String address;
    private String contactNo;

    public Manufacturer() {
        setRole("MANUFACTURER");
        setStatus("PENDING");
    }

    public Manufacturer(int userId, String username, String password, String phoneNo, String pin, String status, Timestamp createdAt,
                        int manufacturerId, String manufacturerName, String address, String contactNo) {
        super(userId, username, password, phoneNo, pin, "MANUFACTURER", status, createdAt);
        this.manufacturerId = manufacturerId;
        this.manufacturerName = manufacturerName;
        this.address = address;
        this.contactNo = contactNo;
    }

    public Manufacturer(String username, String password, String phoneNo, String pin,
                        String manufacturerName, String address, String contactNo) {
        super(username, password, phoneNo, pin, "MANUFACTURER", "PENDING");
        this.manufacturerName = manufacturerName;
        this.address = address;
        this.contactNo = contactNo;
    }

    public int getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(int manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
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
        return "Manufacturer{" +
                "manufacturerId=" + manufacturerId +
                ", userId=" + getUserId() +
                ", username='" + getUsername() + '\'' +
                ", manufacturerName='" + manufacturerName + '\'' +
                ", address='" + address + '\'' +
                ", contactNo='" + contactNo + '\'' +
                ", phoneNo='" + getPhoneNo() + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
    }
}
