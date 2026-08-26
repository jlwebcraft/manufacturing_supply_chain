package manufacturer.qualitycheck.menu;

import manufacturer.productionorder.model.ProductionOrder;
import manufacturer.qualitycheck.model.QualityCheck;
import manufacturer.qualitycheck.service.QualityCheckService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/** Reusable terminal menu for Manufacturer production quality checks. */
public class QualityCheckMenu {
    private final QualityCheckService qualityCheckService = new QualityCheckService();
    private final Scanner scanner = new Scanner(System.in);

    public void showMenu(int manufacturerId) {
        try {
            qualityCheckService.validateManufacturer(manufacturerId);
        } catch (IllegalArgumentException exception) {
            System.out.println("Input error: " + exception.getMessage());
            return;
        } catch (SQLException exception) {
            System.out.println("Database error: " + exception.getMessage());
            return;
        }

        boolean running = true;
        while (running) {
            printMenu();
            try {
                switch (scanner.nextLine().trim()) {
                    case "1": printCompletedOrders(qualityCheckService.viewCompletedOrders(manufacturerId)); break;
                    case "2": performQualityCheck(manufacturerId); break;
                    case "3": printHistory(qualityCheckService.viewQualityCheckHistory(manufacturerId)); break;
                    case "4": running = false; break;
                    default: System.out.println("Invalid choice. Please choose 1 to 4.");
                }
            } catch (IllegalArgumentException exception) {
                System.out.println("Input error: " + exception.getMessage());
            } catch (SQLException exception) {
                System.out.println("Database error: " + exception.getMessage());
            }
        }
    }

    private void performQualityCheck(int manufacturerId) throws SQLException {
        printCompletedOrders(qualityCheckService.viewCompletedOrders(manufacturerId));
        int orderId = readPositiveInt("Completed Production Order ID");
        String result = readResult();
        System.out.print("Remarks (optional): ");
        String remarks = scanner.nextLine();
        int qualityCheckId = qualityCheckService.performQualityCheck(orderId, manufacturerId, result, remarks);
        if ("PASSED".equals(result)) {
            System.out.println("Quality check saved (ID: " + qualityCheckId + "). Order is READY_FOR_SHIPMENT.");
        } else {
            System.out.println("Quality check saved (ID: " + qualityCheckId + "). Order status is FAILED.");
        }
    }

    private String readResult() {
        System.out.print("Quality result (PASSED/FAILED): ");
        String result = scanner.nextLine().trim().toUpperCase();
        if (!"PASSED".equals(result) && !"FAILED".equals(result)) {
            throw new IllegalArgumentException("Enter PASSED or FAILED.");
        }
        return result;
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

    private void printCompletedOrders(List<ProductionOrder> orders) {
        if (orders.isEmpty()) {
            System.out.println("No completed production orders are waiting for quality check.");
            return;
        }
        System.out.printf("%-10s %-22s %-24s %-12s %-16s%n", "Order ID", "Supplier", "Product", "Quantity", "Completed On");
        System.out.println("----------------------------------------------------------------------------------------");
        for (ProductionOrder order : orders) {
            System.out.printf("%-10d %-22s %-24s %-12d %-16s%n", order.getProductionOrderId(),
                    order.getSupplierName(), order.getProductName(), order.getQuantity(), order.getCompletionDate());
        }
    }

    private void printHistory(List<QualityCheck> qualityChecks) {
        if (qualityChecks.isEmpty()) {
            System.out.println("No quality check history found.");
            return;
        }
        System.out.printf("%-10s %-10s %-20s %-22s %-10s %-22s %-10s %-30s%n", "Check ID", "Order ID",
                "Supplier", "Product", "Quantity", "Checked Date", "Result", "Remarks");
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------");
        for (QualityCheck check : qualityChecks) {
            System.out.printf("%-10d %-10d %-20s %-22s %-10d %-22s %-10s %-30s%n", check.getQualityCheckId(),
                    check.getProductionOrderId(), check.getSupplierName(), check.getProductName(), check.getQuantity(),
                    check.getCheckedDate(), check.getResult(), check.getRemarks() == null ? "" : check.getRemarks());
        }
    }

    private void printMenu() {
        System.out.println("\n--- Quality Check Management ---");
        System.out.println("1. View Completed Production Orders");
        System.out.println("2. Perform Quality Check");
        System.out.println("3. View Quality Check History");
        System.out.println("4. Back");
        System.out.print("Enter Choice: ");
    }
}
