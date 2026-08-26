package customer.model;

import java.sql.Timestamp;

public class CustomerShipmentView {
    private int shipmentId;
    private int orderId;
    private String trackingNumber;
    private String shipmentType;
    private String status;
    private Timestamp shippedDate;
    private Timestamp deliveredDate;
    private String supplierName;
    private String customerName;
    private String deliveryAddress;

    public CustomerShipmentView() {
    }

    public CustomerShipmentView(int shipmentId, int orderId, String trackingNumber, String shipmentType,
                                String status, Timestamp shippedDate, Timestamp deliveredDate,
                                String supplierName, String customerName, String deliveryAddress) {
        this.shipmentId = shipmentId;
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.shipmentType = shipmentType;
        this.status = status;
        this.shippedDate = shippedDate;
        this.deliveredDate = deliveredDate;
        this.supplierName = supplierName;
        this.customerName = customerName;
        this.deliveryAddress = deliveryAddress;
    }

    public int getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(int shipmentId) {
        this.shipmentId = shipmentId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getShipmentType() {
        return shipmentType;
    }

    public void setShipmentType(String shipmentType) {
        this.shipmentType = shipmentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getShippedDate() {
        return shippedDate;
    }

    public void setShippedDate(Timestamp shippedDate) {
        this.shippedDate = shippedDate;
    }

    public Timestamp getDeliveredDate() {
        return deliveredDate;
    }

    public void setDeliveredDate(Timestamp deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    @Override
    public String toString() {
        return "CustomerShipmentView{"
                + "shipmentId=" + shipmentId
                + ", orderId=" + orderId
                + ", trackingNumber='" + trackingNumber + '\''
                + ", shipmentType='" + shipmentType + '\''
                + ", status='" + status + '\''
                + ", shippedDate=" + shippedDate
                + ", deliveredDate=" + deliveredDate
                + ", supplierName='" + supplierName + '\''
                + ", customerName='" + customerName + '\''
                + ", deliveryAddress='" + deliveryAddress + '\''
                + '}';
    }
}
