package customer.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class CustomerOrderSummary {
    private int orderId;
    private Timestamp orderDate;
    private String supplierName;
    private BigDecimal totalAmount;
    private String status;

    public CustomerOrderSummary(int orderId, Timestamp orderDate, String supplierName,
                                BigDecimal totalAmount, String status) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.supplierName = supplierName;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }
}
