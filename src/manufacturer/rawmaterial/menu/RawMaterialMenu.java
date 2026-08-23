package manufacturer.rawmaterial.menu;

import manufacturer.rawmaterial.model.RawMaterial;
import manufacturer.rawmaterial.service.RawMaterialService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/** Terminal controller/menu for Raw Material Management. */
public class RawMaterialMenu {
    private final RawMaterialService rawMaterialService = new RawMaterialService();
    private final Scanner scanner = new Scanner(System.in);

    /** Displays Raw Material Management and obtains the manufacturer context. */
    public void showMenu() {
        int manufacturerId;
        try {
            manufacturerId = readPositiveInt("Logged-in Manufacturer ID");
            rawMaterialService.validateManufacturer(manufacturerId);
        } catch (IllegalArgumentException exception) {
            System.out.println("Input error: " + exception.getMessage());
            return;
        } catch (SQLException exception) {
            System.out.println("Database error: " + exception.getMessage());
            return;
        }
        showMenu(manufacturerId);
    }

    /** Allows the shared login/menu module to provide the logged-in manufacturer ID. */
    public void showMenu(int manufacturerId) {
        boolean running = true;
        while (running) {
            printMenu();
            try {
                switch (scanner.nextLine().trim()) {
                    case "1": addRawMaterial(manufacturerId); break;
                    case "2": printMaterials(rawMaterialService.viewMaterialsByStatus(manufacturerId, "ACTIVE")); break;
                    case "3": printMaterials(rawMaterialService.viewMaterialsByStatus(manufacturerId, "INACTIVE")); break;
                    case "4": searchRawMaterial(manufacturerId); break;
                    case "5": updateRawMaterial(manufacturerId); break;
                    case "6": deactivateRawMaterial(manufacturerId); break;
                    case "7": activateRawMaterial(manufacturerId); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice. Please choose 0 to 7");
                }
            } catch (IllegalArgumentException exception) {
                System.out.println("Input error: " + exception.getMessage());
            } catch (SQLException exception) {
                System.out.println("Database error: " + exception.getMessage());
            }
        }
        System.out.println("Leaving Raw Material Management.");
    }

    private void addRawMaterial(int manufacturerId) throws SQLException {
        RawMaterial material = readMaterialDetails(0, manufacturerId);
        int materialId = rawMaterialService.addRawMaterial(manufacturerId, material.getMaterialName(),
                material.getUnit(), material.getMinimumStock());
        System.out.println("Raw material added successfully. ID: " + materialId);
    }

    private void searchRawMaterial(int manufacturerId) throws SQLException {
        System.out.print("Enter material ID, name, or unit: ");
        printMaterials(rawMaterialService.searchRawMaterials(manufacturerId, scanner.nextLine()));
    }

    private void updateRawMaterial(int manufacturerId) throws SQLException {
        int materialId = readPositiveInt("Material ID to update");
        RawMaterial currentMaterial = rawMaterialService.getMaterialForManufacturer(materialId, manufacturerId);
        printMaterialStatus(currentMaterial);
        if ("INACTIVE".equals(currentMaterial.getStatus())) {
            if (readConfirmation("This material is INACTIVE. Do you want to activate it separately? (Y/N): ")) {
                System.out.println("Use Activate Material (option 7) to activate it before updating.");
            } else {
                System.out.println("Update cancelled. The material remains INACTIVE.");
            }
            return;
        }
        RawMaterial material = readMaterialDetails(materialId, manufacturerId);
        System.out.println(rawMaterialService.updateRawMaterial(materialId, manufacturerId,
                material.getMaterialName(), material.getUnit(), material.getMinimumStock())
                ? "Raw material updated successfully." : "Material not found for this manufacturer.");
    }

    private void deactivateRawMaterial(int manufacturerId) throws SQLException {
        printMaterials(rawMaterialService.viewMaterialsByStatus(manufacturerId, "ACTIVE"));
        int materialId = readPositiveInt("Material ID");
        RawMaterial material = rawMaterialService.getMaterialForManufacturer(materialId, manufacturerId);
        if ("INACTIVE".equals(material.getStatus())) {
            throw new IllegalArgumentException("Material is already INACTIVE.");
        }
        printMaterialStatus(material);
        if (!readConfirmation("Are you sure you want to deactivate this material? (Y/N): ")) {
            System.out.println("Deactivation cancelled.");
            return;
        }
        System.out.println(rawMaterialService.deactivateRawMaterial(materialId, manufacturerId)
                ? "Material deactivated successfully." : "Material status could not be changed.");
    }

    private void activateRawMaterial(int manufacturerId) throws SQLException {
        printMaterials(rawMaterialService.viewMaterialsByStatus(manufacturerId, "INACTIVE"));
        int materialId = readPositiveInt("Material ID");
        RawMaterial material = rawMaterialService.getMaterialForManufacturer(materialId, manufacturerId);
        if ("ACTIVE".equals(material.getStatus())) {
            throw new IllegalArgumentException("Material is already ACTIVE.");
        }
        printMaterialStatus(material);
        if (!readConfirmation("Are you sure you want to activate this material? (Y/N): ")) {
            System.out.println("Activation cancelled.");
            return;
        }
        System.out.println(rawMaterialService.activateRawMaterial(materialId, manufacturerId)
                ? "Material activated successfully." : "Material status could not be changed.");
    }

    private RawMaterial readMaterialDetails(int materialId, int manufacturerId) {
        System.out.print("Material name: ");
        String name = scanner.nextLine();
        System.out.print("Unit (for example, kg, litre, piece): ");
        String unit = scanner.nextLine();
        int minimumStock = readNonNegativeInt("Minimum stock");
        return new RawMaterial(materialId, manufacturerId, name, unit, minimumStock, null, 0);
    }

    private int readPositiveInt(String label) {
        int value = readInteger(label);
        if (value <= 0) {
            throw new IllegalArgumentException(label + " must be a positive number.");
        }
        return value;
    }

    private int readNonNegativeInt(String label) {
        int value = readInteger(label);
        if (value < 0) {
            throw new IllegalArgumentException(label + " cannot be negative.");
        }
        return value;
    }

    private int readInteger(String label) {
        System.out.print(label + ": ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }

    private boolean readConfirmation(String prompt) {
        System.out.print(prompt);
        String answer = scanner.nextLine().trim();
        if ("Y".equalsIgnoreCase(answer)) {
            return true;
        }
        if ("N".equalsIgnoreCase(answer)) {
            return false;
        }
        throw new IllegalArgumentException("Please enter Y or N.");
    }

    private void printMaterialStatus(RawMaterial material) {
        System.out.println("Material ID: " + material.getMaterialId());
        System.out.println("Material Name: " + material.getMaterialName());
        System.out.println("Current Status: " + material.getStatus());
    }

    private void printMaterials(List<RawMaterial> materials) {
        if (materials.isEmpty()) {
            System.out.println("No raw materials found.");
            return;
        }
        System.out.printf("%-12s %-25s %-15s %-16s %-18s %-10s%n", "Material ID", "Material Name", "Unit",
                "Minimum Stock", "Current Inventory", "Status");
        System.out.println("------------------------------------------------------------------------------------------------------");
        for (RawMaterial material : materials) {
            System.out.printf("%-12d %-25s %-15s %-16d %-18d %-10s%n", material.getMaterialId(),
                    material.getMaterialName(), material.getUnit(), material.getMinimumStock(),
                    material.getInventoryQuantity(), material.getStatus());
        }
    }

    private void printMenu() {
        System.out.println("\n========================================");
        System.out.println("RAW MATERIAL MANAGEMENT");
        System.out.println("========================================");
        System.out.println("1. Add Raw Material");
        System.out.println("2. View Active Materials");
        System.out.println("3. View Inactive Materials");
        System.out.println("4. Search Material");
        System.out.println("5. Update Material");
        System.out.println("6. Deactivate Material");
        System.out.println("7. Activate Material");
        System.out.println("0. Back");
        System.out.print("Enter Choice: ");
    }
}
