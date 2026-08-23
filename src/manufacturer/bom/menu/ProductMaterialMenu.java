package manufacturer.bom.menu;

import manufacturer.bom.model.ProductMaterialAssignment;
import manufacturer.bom.service.ProductMaterialService;
import manufacturer.product.model.Product;
import manufacturer.rawmaterial.model.RawMaterial;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/** Terminal controller/menu for Bill of Materials management. */
public class ProductMaterialMenu {
    private final ProductMaterialService productMaterialService = new ProductMaterialService();
    private final Scanner scanner = new Scanner(System.in);
    private int selectedProductId;
    private String selectedProductName;

    /** Displays Bill of Materials Management and obtains the manufacturer context. */
    public void showMenu() {
        try {
            int manufacturerId = readPositiveInt("Logged-in Manufacturer ID");
            productMaterialService.validateManufacturer(manufacturerId);
            showMenu(manufacturerId);
        } catch (IllegalArgumentException exception) {
            System.out.println("Input error: " + exception.getMessage());
        } catch (SQLException exception) {
            System.out.println("Database error: " + exception.getMessage());
        }
    }

    /** Allows the shared login/menu module to provide the logged-in manufacturer ID. */
    public void showMenu(int manufacturerId) {
        boolean running = true;
        while (running) {
            printMenu();
            try {
                switch (scanner.nextLine().trim()) {
                    case "1": selectProduct(manufacturerId); break;
                    case "2": viewAssignments(manufacturerId); break;
                    case "3": assignMaterial(manufacturerId); break;
                    case "4": updateQuantity(manufacturerId); break;
                    case "5": removeAssignment(manufacturerId); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice. Please choose 0 to 5.");
                }
            } catch (IllegalArgumentException exception) {
                System.out.println("Input error: " + exception.getMessage());
            } catch (SQLException exception) {
                System.out.println("Database error: " + exception.getMessage());
            }
        }
        selectedProductId = 0;
        selectedProductName = null;
        System.out.println("Leaving Bill of Materials Management.");
    }

    private void selectProduct(int manufacturerId) throws SQLException {
        printProducts(productMaterialService.viewActiveProducts(manufacturerId));
        int productId = readPositiveInt("Product ID");
        Product product = productMaterialService.selectProduct(productId, manufacturerId);
        selectedProductId = product.getProductId();
        selectedProductName = product.getProductName();
        System.out.println("Selected product: " + selectedProductName + " (ID: " + selectedProductId + ")");
    }

    private void viewAssignments(int manufacturerId) throws SQLException {
        ensureProductSelected();
        System.out.println("\nBill of Materials: " + selectedProductName);
        printAssignments(productMaterialService.viewAssignments(selectedProductId, manufacturerId));
    }

    private void assignMaterial(int manufacturerId) throws SQLException {
        ensureProductSelected();
        printMaterials(productMaterialService.viewActiveMaterials(manufacturerId));
        int materialId = readPositiveInt("Material ID");
        int quantityRequired = readPositiveInt("Required quantity");
        System.out.println(productMaterialService.assignMaterial(selectedProductId, materialId, quantityRequired, manufacturerId)
                ? "Raw material assigned successfully." : "Raw material assignment could not be created.");
    }

    private void updateQuantity(int manufacturerId) throws SQLException {
        ensureProductSelected();
        printAssignments(productMaterialService.viewAssignments(selectedProductId, manufacturerId));
        int materialId = readPositiveInt("Material ID");
        int quantityRequired = readPositiveInt("New required quantity");
        System.out.println(productMaterialService.updateRequiredQuantity(selectedProductId, materialId, quantityRequired,
                manufacturerId) ? "Required quantity updated successfully." : "Required quantity could not be updated.");
    }

    private void removeAssignment(int manufacturerId) throws SQLException {
        ensureProductSelected();
        printAssignments(productMaterialService.viewAssignments(selectedProductId, manufacturerId));
        int materialId = readPositiveInt("Material ID to remove");
        System.out.print("Remove this raw material assignment? (Y/N): ");
        if (!"Y".equalsIgnoreCase(scanner.nextLine().trim())) {
            System.out.println("Removal cancelled.");
            return;
        }
        System.out.println(productMaterialService.removeMaterialAssignment(selectedProductId, materialId, manufacturerId)
                ? "Raw material assignment removed successfully." : "Raw material assignment could not be removed.");
    }

    private void ensureProductSelected() {
        if (selectedProductId == 0) {
            throw new IllegalArgumentException("Select a product first (option 1).");
        }
    }

    private int readPositiveInt(String label) {
        System.out.print(label + ": ");
        try {
            int value = Integer.parseInt(scanner.nextLine().trim());
            if (value <= 0) {
                throw new IllegalArgumentException(label + " must be a positive number.");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }

    private void printProducts(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("No active products found.");
            return;
        }
        System.out.printf("%-12s %-30s%n", "Product ID", "Product Name");
        for (Product product : products) {
            System.out.printf("%-12d %-30s%n", product.getProductId(), product.getProductName());
        }
    }

    private void printMaterials(List<RawMaterial> materials) {
        if (materials.isEmpty()) {
            System.out.println("No active raw materials found.");
            return;
        }
        System.out.printf("%-12s %-30s %-15s%n", "Material ID", "Material Name", "Unit");
        for (RawMaterial material : materials) {
            System.out.printf("%-12d %-30s %-15s%n", material.getMaterialId(), material.getMaterialName(),
                    material.getUnit());
        }
    }

    private void printAssignments(List<ProductMaterialAssignment> assignments) {
        if (assignments.isEmpty()) {
            System.out.println("No raw materials are assigned to this product.");
            return;
        }
        System.out.printf("%-30s %-30s %-20s %-15s%n", "Product Name", "Material Name", "Quantity Required", "Unit");
        System.out.println("-------------------------------------------------------------------------------------------------");
        for (ProductMaterialAssignment assignment : assignments) {
            System.out.printf("%-30s %-30s %-20d %-15s%n", assignment.getProductName(),
                    assignment.getMaterialName(), assignment.getQuantityRequired(), assignment.getUnit());
        }
    }

    private void printMenu() {
        System.out.println("\n--- Product-Raw Material Assignment / Bill of Materials ---");
        System.out.println("Selected Product: " + (selectedProductName == null ? "None" : selectedProductName));
        System.out.println("1. Select Product");
        System.out.println("2. View Assigned Raw Materials");
        System.out.println("3. Assign Raw Material");
        System.out.println("4. Update Required Quantity");
        System.out.println("5. Remove Raw Material Assignment");
        System.out.println("0. Back");
        System.out.print("Enter Choice: ");
    }
}
