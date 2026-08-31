package manufacturer.productionorder.menu;

import manufacturer.machine.model.Machine;
import manufacturer.productionorder.model.ProductionOrder;
import manufacturer.productionorder.service.ProductionOrderService;
import manufacturer.worker.model.Worker;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/** Reusable terminal menu for the Manufacturer production-order workflow. */
public class ProductionOrderMenu {
    private final ProductionOrderService productionOrderService = new ProductionOrderService();
    private final Scanner scanner = new Scanner(System.in);

    public void showMenu(int manufacturerId) {
        try {
            productionOrderService.validateManufacturer(manufacturerId);
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
                    case "1": createOrder(manufacturerId); break;
                    case "2": printOrders(productionOrderService.viewProductionOrders(manufacturerId)); break;
                    case "3": viewOrderDetails(manufacturerId); break;
                    case "4": assignWorker(manufacturerId); break;
                    case "5": assignMachine(manufacturerId); break;
                    case "6": startProduction(manufacturerId); break;
                    case "7": completeProduction(manufacturerId); break;
                    case "8": sendForQualityCheck(manufacturerId); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice. Please choose 0 to 8");
                }
            } catch (IllegalArgumentException exception) {
                System.out.println("Input error: " + exception.getMessage());
            } catch (SQLException exception) {
                System.out.println("Database error: " + exception.getMessage());
            }
        }
    }

    private void createOrder(int manufacturerId) throws SQLException {
        int requestId = readPositiveInt("Approved Production Request ID");
        int orderId = productionOrderService.createProductionOrder(requestId, manufacturerId);
        System.out.println("Production order created successfully. ID: " + orderId);
    }

    private void viewOrderDetails(int manufacturerId) throws SQLException {
        int orderId = readPositiveInt("Production Order ID");
        printOrderDetails(productionOrderService.getProductionOrderDetails(orderId, manufacturerId), manufacturerId);
    }

    private void assignWorker(int manufacturerId) throws SQLException {
        printOrders(productionOrderService.viewProductionOrders(manufacturerId));
        int orderId = readPositiveInt("Production Order ID");
        int workerId = readPositiveInt("Available Worker ID");
        System.out.println(productionOrderService.assignWorker(orderId, workerId, manufacturerId)
                ? "Worker assigned successfully." : "Worker could not be assigned.");
    }

    private void assignMachine(int manufacturerId) throws SQLException {
        printOrders(productionOrderService.viewProductionOrders(manufacturerId));
        int orderId = readPositiveInt("Production Order ID");
        int machineId = readPositiveInt("Available Machine ID");
        System.out.println(productionOrderService.assignMachine(orderId, machineId, manufacturerId)
                ? "Machine assigned successfully." : "Machine could not be assigned.");
    }

    private void startProduction(int manufacturerId) throws SQLException {
        int orderId = readPositiveInt("Production Order ID");
        System.out.println(productionOrderService.startProduction(orderId, manufacturerId)
                ? "Production started successfully." : "Production could not be started.");
    }

    private void completeProduction(int manufacturerId) throws SQLException {
        int orderId = readPositiveInt("Production Order ID");
        System.out.println(productionOrderService.completeProduction(orderId, manufacturerId)
                ? "Production completed successfully." : "Production could not be completed.");
    }

    private void sendForQualityCheck(int manufacturerId) throws SQLException {
        int orderId = readPositiveInt("Production Order ID");
        System.out.println(productionOrderService.sendForQualityCheck(orderId, manufacturerId)
                ? "Production order sent for quality check." : "Order could not be sent for quality check.");
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

    private void printOrders(List<ProductionOrder> orders) {
        if (orders.isEmpty()) {
            System.out.println("No production orders found.");
            return;
        }
        System.out.printf("%-10s %-10s %-20s %-20s %-10s %-10s %-15s%n", "Order ID", "Request ID", "Supplier",
                "Product", "Quantity", "Priority", "Status");
        System.out.println("-----------------------------------------------------------------------------------------------------");
        for (ProductionOrder order : orders) {
            System.out.printf("%-10d %-10d %-20s %-20s %-10d %-10s %-15s%n", order.getProductionOrderId(),
                    order.getRequestId(), order.getSupplierName(), order.getProductName(), order.getQuantity(),
                    order.getPriority(), order.getStatus());
        }
    }

    private void printOrderDetails(ProductionOrder order, int manufacturerId) throws SQLException {
        System.out.println("\n--- Production Order Details ---");
        System.out.println("Production Order ID: " + order.getProductionOrderId());
        System.out.println("Request ID: " + order.getRequestId());
        System.out.println("Supplier: " + order.getSupplierName());
        System.out.println("Product: " + order.getProductName());
        System.out.println("Category: " + order.getCategoryName());
        System.out.println("Quantity: " + order.getQuantity());
        System.out.println("Priority: " + order.getPriority());
        System.out.println("Start Date: " + order.getStartDate());
        System.out.println("Completion Date: " + order.getCompletionDate());
        System.out.println("Status: " + order.getStatus());
        printWorkers(productionOrderService.getAssignedWorkers(order.getProductionOrderId(), manufacturerId));
        printMachines(productionOrderService.getAssignedMachines(order.getProductionOrderId(), manufacturerId));
    }

    private void printWorkers(List<Worker> workers) {
        System.out.println("Assigned Workers:");
        if (workers.isEmpty()) {
            System.out.println("  None");
            return;
        }
        for (Worker worker : workers) {
            System.out.println("  " + worker.getWorkerId() + " - " + worker.getWorkerName() + " ("
                    + worker.getSkill() + ", " + worker.getStatus() + ")");
        }
    }

    private void printMachines(List<Machine> machines) {
        System.out.println("Assigned Machines:");
        if (machines.isEmpty()) {
            System.out.println("  None");
            return;
        }
        for (Machine machine : machines) {
            System.out.println("  " + machine.getMachineId() + " - " + machine.getMachineName() + " ("
                    + machine.getMachineType() + ", " + machine.getStatus() + ")");
        }
    }

    private void printMenu() {
        System.out.println("\n--- Manufacturer Production Order Management ---");
        System.out.println("1. Create Production Order");
        System.out.println("2. View Production Orders");
        System.out.println("3. View Production Order Details");
        System.out.println("4. Assign Worker");
        System.out.println("5. Assign Machine");
        System.out.println("6. Start Production");
        System.out.println("7. Complete Production");
        System.out.println("8. Send for Quality Check");
        System.out.println("0. Back");
        System.out.print("Enter Choice: ");
    }
}
