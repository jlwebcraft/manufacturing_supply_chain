package controller;

import exception.DatabaseException;
import exception.InvalidInputException;
import model.Manufacturer;
import model.Supplier;
import service.RegistrationService;

import java.util.Scanner;

/**
 * Controller managing Registration menu and workflows for Manufacturers and Suppliers.
 */
public class RegistrationController {

    private final RegistrationService registrationService;
    private final Scanner scanner;

    public RegistrationController() {
        this.registrationService = new RegistrationService();
        this.scanner = new Scanner(System.in);
    }

    public RegistrationController(RegistrationService registrationService, Scanner scanner) {
        this.registrationService = registrationService;
        this.scanner = scanner;
    }

    public void startMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n========== REGISTRATION ==========");
            System.out.println("1. Manufacturer Registration");
            System.out.println("2. Supplier Registration");
            System.out.println("3. Customer Registration");
            System.out.println("0. Back");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleManufacturerRegistration();
                    break;
                case "2":
                    handleSupplierRegistration();
                    break;
                case "3":
                    System.out.println("\n[Integration Point] Customer Registration belongs to Member 4's Customer module.");
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter 1, 2, 3, or 0.");
            }
        }
    }

    private void handleManufacturerRegistration() {
        System.out.println("\n--- MANUFACTURER REGISTRATION ---");
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        System.out.print("Enter Phone Number: ");
        String phoneNo = scanner.nextLine();
        System.out.print("Enter PIN (4-6 digits): ");
        String pin = scanner.nextLine();
        System.out.print("Enter Manufacturer/Company Name: ");
        String mfgName = scanner.nextLine();
        System.out.print("Enter Business Address: ");
        String address = scanner.nextLine();
        System.out.print("Enter Contact Number: ");
        String contactNo = scanner.nextLine();

        try {
            Manufacturer manufacturer = registrationService.registerManufacturer(
                    username, password, phoneNo, pin, mfgName, address, contactNo);
            System.out.println("\nSUCCESS: Manufacturer registered successfully!");
            System.out.println("Assigned User ID: " + manufacturer.getUserId() + " | Manufacturer ID: " + manufacturer.getManufacturerId());
            System.out.println("Status: PENDING (Requires Admin review and approval before login)");
        } catch (InvalidInputException | DatabaseException e) {
            System.err.println("Registration failed: " + e.getMessage());
        }
    }

    private void handleSupplierRegistration() {
        System.out.println("\n--- SUPPLIER REGISTRATION ---");
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        System.out.print("Enter Phone Number: ");
        String phoneNo = scanner.nextLine();
        System.out.print("Enter PIN (4-6 digits): ");
        String pin = scanner.nextLine();
        System.out.print("Enter Supplier/Company Name: ");
        String supplierName = scanner.nextLine();
        System.out.print("Enter Business Address: ");
        String address = scanner.nextLine();
        System.out.print("Enter Contact Number: ");
        String contactNo = scanner.nextLine();

        try {
            Supplier supplier = registrationService.registerSupplier(
                    username, password, phoneNo, pin, supplierName, address, contactNo);
            System.out.println("\nSUCCESS: Supplier registered successfully!");
            System.out.println("Assigned User ID: " + supplier.getUserId() + " | Supplier ID: " + supplier.getSupplierId());
            System.out.println("Status: PENDING (Requires Admin review and approval before login)");
        } catch (InvalidInputException | DatabaseException e) {
            System.err.println("Registration failed: " + e.getMessage());
        }
    }
}
