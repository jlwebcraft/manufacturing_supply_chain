package manufacturer.qualitycheck.model;

import java.sql.Timestamp;

/** Manufacturer-facing quality check record with production-order display information. */
public class QualityCheck {
    private final int qualityCheckId;
    private final int productionOrderId;
    private final String supplierName;
    private final String productName;
    private final int quantity;
    private final Timestamp checkedDate;
    private final String result;
    private final String remarks;

    public QualityCheck(int qualityCheckId, int productionOrderId, String supplierName, String productName,
                        int quantity, Timestamp checkedDate, String result, String remarks) {
        this.qualityCheckId = qualityCheckId;
        this.productionOrderId = productionOrderId;
        this.supplierName = supplierName;
        this.productName = productName;
        this.quantity = quantity;
        this.checkedDate = checkedDate;
        this.result = result;
        this.remarks = remarks;
    }

    public int getQualityCheckId() { return qualityCheckId; }
    public int getProductionOrderId() { return productionOrderId; }
    public String getSupplierName() { return supplierName; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public Timestamp getCheckedDate() { return checkedDate; }
    public String getResult() { return result; }
    public String getRemarks() { return remarks; }
}
