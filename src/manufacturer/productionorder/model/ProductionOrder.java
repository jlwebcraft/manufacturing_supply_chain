package manufacturer.productionorder.model;

import java.sql.Date;

/** Manufacturer-facing production order with supplier, product, and category display data. */
public class ProductionOrder {
    private final int productionOrderId;
    private final int requestId;
    private final int manufacturerId;
    private final String supplierName;
    private final String productName;
    private final String categoryName;
    private final int quantity;
    private final String priority;
    private final Date startDate;
    private final Date completionDate;
    private final String status;

    public ProductionOrder(int productionOrderId, int requestId, int manufacturerId, String supplierName,
                           String productName, String categoryName, int quantity, String priority,
                           Date startDate, Date completionDate, String status) {
        this.productionOrderId = productionOrderId;
        this.requestId = requestId;
        this.manufacturerId = manufacturerId;
        this.supplierName = supplierName;
        this.productName = productName;
        this.categoryName = categoryName;
        this.quantity = quantity;
        this.priority = priority;
        this.startDate = startDate;
        this.completionDate = completionDate;
        this.status = status;
    }

    public int getProductionOrderId() { return productionOrderId; }
    public int getRequestId() { return requestId; }
    public int getManufacturerId() { return manufacturerId; }
    public String getSupplierName() { return supplierName; }
    public String getProductName() { return productName; }
    public String getCategoryName() { return categoryName; }
    public int getQuantity() { return quantity; }
    public String getPriority() { return priority; }
    public Date getStartDate() { return startDate; }
    public Date getCompletionDate() { return completionDate; }
    public String getStatus() { return status; }
}
