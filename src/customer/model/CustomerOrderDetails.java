package customer.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrderDetails {
    private int orderId;
    private Timestamp orderDate;
    private String supplierName;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemDetail> items;

    public CustomerOrderDetails(int orderId, Timestamp orderDate, String supplierName,
                                BigDecimal totalAmount, String status) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.supplierName = supplierName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.items = new ArrayList<>();
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

    public List<OrderItemDetail> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDetail> items) {
        this.items = items;
    }
}
