package manufacturer.inventory.model;

import java.sql.Timestamp;

/** Read-only transaction-history view for manufacturer material inventory. */
public class InventoryTransaction {
    public final int transactionId, inventoryId, materialId, quantity, referenceId;
    public final String materialName, transactionType, referenceType;
    public final Timestamp transactionDate;
    public InventoryTransaction(int transactionId, int inventoryId, int materialId, String materialName, String transactionType,
                                int quantity, String referenceType, int referenceId, Timestamp transactionDate) {
        this.transactionId = transactionId; this.inventoryId = inventoryId; this.materialId = materialId;
        this.materialName = materialName; this.transactionType = transactionType; this.quantity = quantity;
        this.referenceType = referenceType; this.referenceId = referenceId; this.transactionDate = transactionDate;
    }
}
