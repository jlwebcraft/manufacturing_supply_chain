package customer.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class CustomerOrder {
    private int orderId;
    private int customerId;
    private int supplierId;
    private Timestamp orderDate;
    private BigDecimal totalAmount;
    private String status;

    public CustomerOrder() {
    }

    public CustomerOrder(int customerId, int supplierId, BigDecimal totalAmount, String status) {
        this.customerId = customerId;
        this.supplierId = supplierId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public CustomerOrder(int orderId, int customerId, int supplierId, Timestamp orderDate,
                         BigDecimal totalAmount, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.supplierId = supplierId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CustomerOrder{"
                + "orderId=" + orderId
                + ", customerId=" + customerId
                + ", supplierId=" + supplierId
                + ", orderDate=" + orderDate
                + ", totalAmount=" + totalAmount
                + ", status='" + status + '\''
                + '}';
    }
}
