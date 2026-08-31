package com.mscm.supplier;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * Team integration point: call new SupplierMenu(scanner, loggedInSupplierId).start();
 * The login module must pass the supplier_id, not user_id.
 */
public class SupplierMenu {
    private final Scanner scanner;
    private final SupplierService service;

    public SupplierMenu(Scanner scanner, int loggedInSupplierId) {
        this.scanner = scanner;
        this.service = new SupplierService(loggedInSupplierId);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n========== SUPPLIER MENU ==========");
            System.out.println("1. View / Update Profile");
            System.out.println("2. Product Catalog");
            System.out.println("3. Production Requests");
            System.out.println("4. Received Products");
            System.out.println("5. Inventory");
            System.out.println("6. Customer Orders");
            System.out.println("7. Sales");
            System.out.println("8. Customer Shipments");
            System.out.println("9. Logout");
            System.out.print("Choose an option: ");
            try {
                switch (InputValidator.menuChoice(scanner, 1, 9)) {
                    case 1: profileMenu(); break;
                    case 2: catalogMenu(); break;
                    case 3: productionRequestMenu(); break;
                    case 4: service.viewReceivedProducts(); break;
                    case 5: inventoryMenu(); break;
                    case 6: orderMenu(); break;
                    case 7: salesMenu(); break;
                    case 8: shipmentMenu(); break;
                    case 9: running = false; break;
                    default: break;
                }
            } catch (SQLException ex) {
                System.out.println("Database operation failed: " + ex.getMessage());
            }
        }
    }

    private void profileMenu() throws SQLException {
        service.viewProfile();
        System.out.print("Update this profile? (Y/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) return;
        String name = InputValidator.requiredText(scanner, "Supplier name", true);
        String address = InputValidator.requiredText(scanner, "Address", false);
        String phone = InputValidator.phone(scanner, "Contact number");
        service.updateProfile(name, address, phone);
    }

    private void catalogMenu() throws SQLException {
        System.out.println("\n1. View active products\n2. Search by product name\n3. Search by category\n4. Product details\n0. Back");
        System.out.print("Choose: ");
        switch (InputValidator.menuChoice(scanner, 0, 4)) {
            case 1: service.viewActiveProducts(); break;
            case 2: service.searchProducts(InputValidator.requiredText(scanner, "Product name", false), false); break;
            case 3: service.searchProducts(InputValidator.requiredText(scanner, "Category name", false), true); break;
            case 4: { int id = InputValidator.positiveInt(scanner, "Product ID", true); if (id != 0) service.viewProductDetails(id); break; }
            default: break;
        }
    }

    private void productionRequestMenu() throws SQLException {
        System.out.println("\n1. Create production request\n2. View my production requests\n0. Back");
        System.out.print("Choose: ");
        switch (InputValidator.menuChoice(scanner, 0, 2)) {
            case 1: createProductionRequest(); break;
            case 2: service.viewProductionRequests(); break;
            default: break;
        }
    }

    private void createProductionRequest() throws SQLException {
        service.viewActiveProducts();
        int productId = InputValidator.positiveInt(scanner, "Product ID", true);
        if (productId == 0) return;
        int manufacturerId = InputValidator.positiveInt(scanner, "Manufacturer ID", true);
        if (manufacturerId == 0) return;
        int quantity = InputValidator.positiveInt(scanner, "Quantity", false);
        String priority = InputValidator.priority(scanner);
        service.createProductionRequest(productId, manufacturerId, quantity, priority,
                InputValidator.futureOrTodayDate(scanner, "Required date"));
    }

    private void inventoryMenu() throws SQLException {
        System.out.println("\n1. View inventory\n2. View inventory transactions\n0. Back");
        System.out.print("Choose: ");
        int choice = InputValidator.menuChoice(scanner, 0, 2);
        if (choice == 1) service.viewInventory();
        else if (choice == 2) service.viewInventoryTransactions();
    }

    private void orderMenu() throws SQLException {
        System.out.println("\n1. View customer orders\n2. View order items\n3. Confirm order and create sale\n4. Reject order\n5. Mark order as processing\n6. Cancel order\n0. Back");
        System.out.print("Choose: ");
        int choice = InputValidator.menuChoice(scanner, 0, 6);
        if (choice == 1) service.viewCustomerOrders();
        else if (choice == 2) showOrderItems();
        else if (choice >= 3 && choice <= 6) updateOrder(choice);
    }

    private void showOrderItems() throws SQLException {
        int orderId = InputValidator.positiveInt(scanner, "Order ID", true);
        if (orderId != 0) service.viewOrderItems(orderId);
    }

    private void updateOrder(int choice) throws SQLException {
        int orderId = InputValidator.positiveInt(scanner, "Order ID", true);
        if (orderId == 0) return;
        if (choice == 3) service.confirmOrderAndCreateSale(orderId);
        else if (choice == 4) service.updateOrderStatus(orderId, "REJECTED");
        else if (choice == 5) service.updateOrderStatus(orderId, "PROCESSING");
        else service.updateOrderStatus(orderId, "CANCELLED");
    }

    private void salesMenu() throws SQLException {
        System.out.println("\n1. View sales\n2. Complete sale\n0. Back");
        System.out.print("Choose: ");
        int choice = InputValidator.menuChoice(scanner, 0, 2);
        if (choice == 1) service.viewSales();
        else if (choice == 2) { int id = InputValidator.positiveInt(scanner, "Sale ID", true); if (id != 0) service.updateSaleStatus(id, "COMPLETED"); }
    }

    private void shipmentMenu() throws SQLException {
        System.out.println("\n1. View customer shipments\n2. Create shipment\n3. Dispatch shipment\n4. Mark in transit\n5. Mark delivered\n6. Cancel shipment\n0. Back");
        System.out.print("Choose: ");
        int choice = InputValidator.menuChoice(scanner, 0, 6);
        if (choice == 1) service.viewCustomerShipments();
        else if (choice == 2) { int orderId = InputValidator.positiveInt(scanner, "Order ID", true); if (orderId != 0) service.createCustomerShipment(orderId, InputValidator.futureOrTodayDate(scanner, "Shipment date")); }
        else if (choice >= 3) { int id = InputValidator.positiveInt(scanner, "Shipment ID", true); if (id != 0) service.updateShipmentStatus(id, new String[]{"", "", "", "DISPATCHED", "IN_TRANSIT", "DELIVERED", "CANCELLED"}[choice]); }
    }
}
