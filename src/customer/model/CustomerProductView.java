package customer.model;

import java.math.BigDecimal;

public class CustomerProductView {
    private int productId;
    private String productName;
    private String categoryName;
    private String description;
    private BigDecimal unitPrice;
    private String manufacturerName;

    public CustomerProductView() {
    }

    public CustomerProductView(int productId, String productName, String categoryName,
                               String description, BigDecimal unitPrice, String manufacturerName) {
        this.productId = productId;
        this.productName = productName;
        this.categoryName = categoryName;
        this.description = description;
        this.unitPrice = unitPrice;
        this.manufacturerName = manufacturerName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    @Override
    public String toString() {
        return "CustomerProductView{"
                + "productId=" + productId
                + ", productName='" + productName + '\''
                + ", categoryName='" + categoryName + '\''
                + ", description='" + description + '\''
                + ", unitPrice=" + unitPrice
                + ", manufacturerName='" + manufacturerName + '\''
                + '}';
    }
}
