package manufacturer.inventory.model;

/** Read-only manufacturer material inventory view. */
public class ManufacturerInventory {
    public final int inventoryId, materialId, quantity, minimumStock;
    public final String materialName, unit;
    public ManufacturerInventory(int inventoryId, int materialId, String materialName, String unit, int quantity, int minimumStock) {
        this.inventoryId = inventoryId; this.materialId = materialId; this.materialName = materialName;
        this.unit = unit; this.quantity = quantity; this.minimumStock = minimumStock;
    }
}
