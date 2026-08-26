package manufacturer.productionrequest.model;

import java.sql.Date;
import java.sql.Timestamp;

/** Manufacturer-facing view of a supplier-created production request. */
public class ProductionRequest {
    private final int requestId;
    private final int supplierId;
    private final int manufacturerId;
    private final int productId;
    private final String supplierName;
    private final String productName;
    private final String categoryName;
    private final int quantity;
    private final String priority;
    private final Date requiredDate;
    private final Timestamp requestDate;
    private final String status;

    public ProductionRequest(int requestId, int supplierId, int manufacturerId, int productId, String supplierName,
                             String productName, String categoryName, int quantity, String priority,
                             Date requiredDate, Timestamp requestDate, String status) {
        this.requestId = requestId;
        this.supplierId = supplierId;
        this.manufacturerId = manufacturerId;
        this.productId = productId;
        this.supplierName = supplierName;
        this.productName = productName;
        this.categoryName = categoryName;
        this.quantity = quantity;
        this.priority = priority;
        this.requiredDate = requiredDate;
        this.requestDate = requestDate;
        this.status = status;
    }

    public int getRequestId() { return requestId; }
    public int getSupplierId() { return supplierId; }
    public int getManufacturerId() { return manufacturerId; }
    public int getProductId() { return productId; }
    public String getSupplierName() { return supplierName; }
    public String getProductName() { return productName; }
    public String getCategoryName() { return categoryName; }
    public int getQuantity() { return quantity; }
    public String getPriority() { return priority; }
    public Date getRequiredDate() { return requiredDate; }
    public Timestamp getRequestDate() { return requestDate; }
    public String getStatus() { return status; }
}
