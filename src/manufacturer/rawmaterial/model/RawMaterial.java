package manufacturer.rawmaterial.model;

/** Represents a raw material and its current manufacturer inventory quantity. */
public class RawMaterial {
    private int materialId;
    private int manufacturerId;
    private String materialName;
    private String unit;
    private int minimumStock;
    private String status;
    private int inventoryQuantity;

    public RawMaterial() {
    }

    public RawMaterial(int manufacturerId, String materialName, String unit, int minimumStock) {
        this.manufacturerId = manufacturerId;
        this.materialName = materialName;
        this.unit = unit;
        this.minimumStock = minimumStock;
    }

    public RawMaterial(int materialId, int manufacturerId, String materialName, String unit,
                       int minimumStock, String status, int inventoryQuantity) {
        this.materialId = materialId;
        this.manufacturerId = manufacturerId;
        this.materialName = materialName;
        this.unit = unit;
        this.minimumStock = minimumStock;
        this.status = status;
        this.inventoryQuantity = inventoryQuantity;
    }

    public int getMaterialId() { return materialId; }
    public void setMaterialId(int materialId) { this.materialId = materialId; }
    public int getManufacturerId() { return manufacturerId; }
    public void setManufacturerId(int manufacturerId) { this.manufacturerId = manufacturerId; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public int getMinimumStock() { return minimumStock; }
    public void setMinimumStock(int minimumStock) { this.minimumStock = minimumStock; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getInventoryQuantity() { return inventoryQuantity; }
    public void setInventoryQuantity(int inventoryQuantity) { this.inventoryQuantity = inventoryQuantity; }
}
