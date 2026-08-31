package manufacturer.product.model;

import java.math.BigDecimal;

/** Represents a product and its category name when loaded through a JOIN. */
public class Product {
    private int productId;
    private int manufacturerId;
    private int categoryId;
    private String productName;
    private String description;
    private BigDecimal price;
    private String status;
    private String categoryName;

    public Product() {
    }

    public Product(int manufacturerId, int categoryId, String productName, String description, BigDecimal price) {
        this.manufacturerId = manufacturerId;
        this.categoryId = categoryId;
        this.productName = productName;
        this.description = description;
        this.price = price;
    }

    public Product(int productId, int manufacturerId, int categoryId, String productName,
                   String description, BigDecimal price, String status, String categoryName) {
        this.productId = productId;
        this.manufacturerId = manufacturerId;
        this.categoryId = categoryId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.status = status;
        this.categoryName = categoryName;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getManufacturerId() { return manufacturerId; }
    public void setManufacturerId(int manufacturerId) { this.manufacturerId = manufacturerId; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
