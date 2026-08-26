package customer.model;

import java.math.BigDecimal;

public class SupplierProductStockView {
    private int supplierId;
    private String supplierName;
    private int productId;
    private BigDecimal availableQuantity;

    public SupplierProductStockView() {
    }

    public SupplierProductStockView(int supplierId, String supplierName, int productId,
                                    BigDecimal availableQuantity) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.productId = productId;
        this.availableQuantity = availableQuantity;
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

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(BigDecimal availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}
