package customer.model;

import java.math.BigDecimal;

public class OrderPlacementResult {
    private int orderId;
    private BigDecimal expectedTotal;
    private BigDecimal databaseTotal;
    private String status;

    public OrderPlacementResult(int orderId, BigDecimal expectedTotal, BigDecimal databaseTotal, String status) {
        this.orderId = orderId;
        this.expectedTotal = expectedTotal;
        this.databaseTotal = databaseTotal;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public BigDecimal getExpectedTotal() {
        return expectedTotal;
    }

    public BigDecimal getDatabaseTotal() {
        return databaseTotal;
    }

    public String getStatus() {
        return status;
    }
}
