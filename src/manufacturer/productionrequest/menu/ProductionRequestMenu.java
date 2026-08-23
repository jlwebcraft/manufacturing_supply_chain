package manufacturer.productionrequest.menu;

import manufacturer.productionrequest.model.ProductionRequest;
import manufacturer.productionrequest.service.ProductionRequestService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/** Reusable terminal menu for Manufacturer-side Production Request Management. */
public class ProductionRequestMenu {
    private final ProductionRequestService productionRequestService = new ProductionRequestService();
    private final Scanner scanner = new Scanner(System.in);

    public void showMenu(int manufacturerId) {
        try {
            productionRequestService.validateManufacturer(manufacturerId);
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
                    case "1": printRequests(productionRequestService.viewPendingRequests(manufacturerId)); break;
                    case "2": viewRequestDetails(manufacturerId); break;
                    case "3": approveRequest(manufacturerId); break;
                    case "4": rejectRequest(manufacturerId); break;
                    case "5": printRequests(productionRequestService.viewRequestHistory(manufacturerId)); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice. Please choose 1 to 6.");
                }
            } catch (IllegalArgumentException exception) {
                System.out.println("Input error: " + exception.getMessage());
            } catch (SQLException exception) {
                System.out.println("Database error: " + exception.getMessage());
            }
        }
    }

    private void viewRequestDetails(int manufacturerId) throws SQLException {
        ProductionRequest request = productionRequestService.getRequestDetails(readPositiveInt("Request ID"), manufacturerId);
        printRequestDetails(request);
    }

    private void approveRequest(int manufacturerId) throws SQLException {
        printRequests(productionRequestService.viewPendingRequests(manufacturerId));
        int requestId = readPositiveInt("Request ID to approve");
        ProductionRequest request = productionRequestService.getRequestDetails(requestId, manufacturerId);
        printRequestDetails(request);
        if (!readConfirmation("Approve this production request? (Y/N): ")) {
            System.out.println("Approval cancelled.");
            return;
        }
        productionRequestService.approveRequest(requestId, manufacturerId);
        System.out.println("Request approved successfully. Create a production order as the next step.");
    }

    private void rejectRequest(int manufacturerId) throws SQLException {
        printRequests(productionRequestService.viewPendingRequests(manufacturerId));
        int requestId = readPositiveInt("Request ID to reject");
        ProductionRequest request = productionRequestService.getRequestDetails(requestId, manufacturerId);
        printRequestDetails(request);
        if (!readConfirmation("Reject this production request? (Y/N): ")) {
            System.out.println("Rejection cancelled.");
            return;
        }
        productionRequestService.rejectRequest(requestId, manufacturerId);
        System.out.println("Request rejected successfully.");
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

    private void printRequests(List<ProductionRequest> requests) {
        if (requests.isEmpty()) {
            System.out.println("No production requests found.");
            return;
        }
        System.out.printf("%-10s %-22s %-22s %-18s %-10s %-10s %-15s %-21s %-10s%n", "Request ID",
                "Supplier", "Product", "Category", "Quantity", "Priority", "Required Date", "Request Date", "Status");
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------");
        for (ProductionRequest request : requests) {
            System.out.printf("%-10d %-22s %-22s %-18s %-10d %-10s %-15s %-21s %-10s%n", request.getRequestId(),
                    request.getSupplierName(), request.getProductName(), request.getCategoryName(), request.getQuantity(),
                    request.getPriority(), request.getRequiredDate(), request.getRequestDate(), request.getStatus());
        }
    }

    private void printRequestDetails(ProductionRequest request) {
        System.out.println("\n--- Production Request Details ---");
        System.out.println("Request ID: " + request.getRequestId());
        System.out.println("Supplier Name: " + request.getSupplierName());
        System.out.println("Product Name: " + request.getProductName());
        System.out.println("Category: " + request.getCategoryName());
        System.out.println("Quantity: " + request.getQuantity());
        System.out.println("Priority: " + request.getPriority());
        System.out.println("Required Date: " + request.getRequiredDate());
        System.out.println("Request Date: " + request.getRequestDate());
        System.out.println("Status: " + request.getStatus());
    }

    private void printMenu() {
        System.out.println("\n--- Production Request Management ---");
        System.out.println("1. View Pending Requests");
        System.out.println("2. View Request Details");
        System.out.println("3. Approve Request");
        System.out.println("4. Reject Request");
        System.out.println("5. View Request History");
        System.out.println("0. Back");
        System.out.print("Enter Choice: ");
    }
}
