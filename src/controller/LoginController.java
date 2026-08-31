package controller;

import exception.AuthenticationException;
import exception.DatabaseException;
import exception.InvalidInputException;
import model.Manufacturer;
import model.Supplier;
import model.User;
import service.AuthenticationService;

import java.util.Scanner;

/**
 * Main Controller for Authentication, Navigation, and Role-Based Handoff.
 */
public class LoginController {

    private final AuthenticationService authService;
    private final AdminController adminController;
    private final RegistrationController registrationController;
    private final Scanner scanner;

    public LoginController() {
        this.authService = new AuthenticationService();
        this.adminController = new AdminController();
        this.registrationController = new RegistrationController();
        this.scanner = new Scanner(System.in);
    }

    public LoginController(AuthenticationService authService, AdminController adminController,
                           RegistrationController registrationController, Scanner scanner) {
        this.authService = authService;
        this.adminController = adminController;
        this.registrationController = registrationController;
        this.scanner = scanner;
    }

    public void startMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n========================================");
            System.out.println(" MANUFACTURING & SUPPLY CHAIN MANAGEMENT");
            System.out.println("========================================");
            System.out.println("1. Admin Login");
            System.out.println("2. Manufacturer Login");
            System.out.println("3. Supplier Login");
            System.out.println("4. Customer Login");
            System.out.println("5. Register");
            System.out.println("0. Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleAdminLogin();
                    break;
                case "2":
                    handleManufacturerLogin();
                    break;
                case "3":
                    handleSupplierLogin();
                    break;
                case "4":
                    handleCustomerLogin();
                    break;
                case "5":
                    registrationController.startMenu();
                    break;
                case "0":
                    System.out.println("\nThank you for using Manufacturing & Supply Chain Management System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number between 0 and 5.");
            }
        }
    }

    private void handleAdminLogin() {
        System.out.println("\n--- ADMIN LOGIN ---");
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        try {
            User admin = authService.loginAdmin(username, password);
            System.out.println("\nLOGIN SUCCESSFUL! Admin authenticated: " + admin.getUsername());
            adminController.startMenu(admin);
        } catch (AuthenticationException | InvalidInputException | DatabaseException e) {
            System.err.println("Login Failed: " + e.getMessage());
        }
    }

    private void handleManufacturerLogin() {
        System.out.println("\n--- MANUFACTURER LOGIN ---");
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        try {
            Manufacturer manufacturer = authService.loginManufacturer(username, password);
            System.out.println("\nLOGIN SUCCESSFUL! Welcome Manufacturer: " + manufacturer.getManufacturerName());
            System.out.println("[Integration Point] Passing control to Member 2's ManufacturerController...");
            // Integration Hook:
            // new ManufacturerController(manufacturer).startMenu();
        } catch (AuthenticationException | InvalidInputException | DatabaseException e) {
            System.err.println("Login Failed: " + e.getMessage());
        }
    }

    private void handleSupplierLogin() {
        System.out.println("\n--- SUPPLIER LOGIN ---");
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        try {
            Supplier supplier = authService.loginSupplier(username, password);
            System.out.println("\nLOGIN SUCCESSFUL! Welcome Supplier: " + supplier.getSupplierName());
            System.out.println("[Integration Point] Passing control to Member 3's SupplierController...");
            // Integration Hook:
            // new SupplierController(supplier).startMenu();
        } catch (AuthenticationException | InvalidInputException | DatabaseException e) {
            System.err.println("Login Failed: " + e.getMessage());
        }
    }

    private void handleCustomerLogin() {
        System.out.println("\n--- CUSTOMER LOGIN ---");
        System.out.print("Enter Phone Number: ");
        String phoneNo = scanner.nextLine();
        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine();

        try {
            User customer = authService.loginCustomer(phoneNo, pin);
            System.out.println("\nLOGIN SUCCESSFUL! Welcome Customer (Phone: " + customer.getPhoneNo() + ")");
            System.out.println("[Integration Point] Passing control to Member 4's CustomerController...");
            // Integration Hook:
            // new CustomerController(customer).startMenu();
        } catch (AuthenticationException | InvalidInputException | DatabaseException e) {
            System.err.println("Login Failed: " + e.getMessage());
        }
    }
}
