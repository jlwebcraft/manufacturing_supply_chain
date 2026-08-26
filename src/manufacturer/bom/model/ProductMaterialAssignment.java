package manufacturer.bom.model;

/** Represents one Bill of Materials row from product_materials and its display names. */
public class ProductMaterialAssignment {
    private final int productId;
    private final String productName;
    private final int materialId;
    private final String materialName;
    private final int quantityRequired;
    private final String unit;

    public ProductMaterialAssignment(int productId, String productName, int materialId,
                                     String materialName, int quantityRequired, String unit) {
        this.productId = productId;
        this.productName = productName;
        this.materialId = materialId;
        this.materialName = materialName;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
    }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getMaterialId() { return materialId; }
    public String getMaterialName() { return materialName; }
    public int getQuantityRequired() { return quantityRequired; }
    public String getUnit() { return unit; }
}
