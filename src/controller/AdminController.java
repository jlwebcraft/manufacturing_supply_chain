package controller;

import exception.DatabaseException;
import exception.UserNotFoundException;
import model.Manufacturer;
import model.Supplier;
import model.User;
import service.AdminService;

import java.util.List;
import java.util.Scanner;

/**
 * Controller for Admin operations and menu management.
 */
public class AdminController {

    private final AdminService adminService;
    private final Scanner scanner;

    public AdminController() {
        this.adminService = new AdminService();
        this.scanner = new Scanner(System.in);
    }

    public AdminController(AdminService adminService, Scanner scanner) {
        this.adminService = adminService;
        this.scanner = scanner;
    }

    public void startMenu(User adminUser) {
        System.out.println("\nWelcome, Admin " + adminUser.getUsername() + "!");
        boolean running = true;

        while (running) {
            System.out.println("\n========== ADMIN MENU ==========");
            System.out.println("1. View Pending Manufacturers");
            System.out.println("2. Approve Manufacturer");
            System.out.println("3. Reject Manufacturer");
            System.out.println("4. View Pending Suppliers");
            System.out.println("5. Approve Supplier");
            System.out.println("6. Reject Supplier");
            System.out.println("7. View All Users");
            System.out.println("8. Search User");
            System.out.println("9. Deactivate User");
            System.out.println("10. View User Details");
            System.out.println("0. Logout");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleViewPendingManufacturers();
                    break;
                case "2":
                    handleApproveManufacturer();
                    break;
                case "3":
                    handleRejectManufacturer();
                    break;
                case "4":
                    handleViewPendingSuppliers();
                    break;
                case "5":
                    handleApproveSupplier();
                    break;
                case "6":
                    handleRejectSupplier();
                    break;
                case "7":
                    handleViewAllUsers();
                    break;
                case "8":
                    handleSearchUser();
                    break;
                case "9":
                    handleDeactivateUser();
                    break;
                case "10":
                    handleViewUserDetails();
                    break;
                case "0":
                    System.out.println("Logging out Admin session...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please select a number between 0 and 10.");
            }
        }
    }

    private void handleViewPendingManufacturers() {
        try {
            List<Manufacturer> pending = adminService.viewPendingManufacturers();
            System.out.println("\n----- PENDING MANUFACTURERS -----");
            if (pending.isEmpty()) {
                System.out.println("No pending manufacturer registrations found.");
                return;
            }
            for (Manufacturer m : pending) {
                System.out.printf("Manufacturer ID: %d | User ID: %d | Name: %s | Address: %s | Contact: %s | Phone: %s | Status: %s%n",
                        m.getManufacturerId(), m.getUserId(), m.getManufacturerName(),
                        m.getAddress(), m.getContactNo(), m.getPhoneNo(), m.getStatus());
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading pending manufacturers: " + e.getMessage());
        }
    }

    private void handleApproveManufacturer() {
        System.out.print("Enter Manufacturer ID to approve: ");
        String input = scanner.nextLine().trim();
        try {
            int id = Integer.parseInt(input);
            if (adminService.approveManufacturer(id)) {
                System.out.println("SUCCESS: Manufacturer ID " + id + " has been APPROVED.");
            } else {
                System.out.println("Failed to approve manufacturer.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Must be an integer.");
        } catch (UserNotFoundException | DatabaseException e) {
            System.err.println("Error approving manufacturer: " + e.getMessage());
        }
    }

    private void handleRejectManufacturer() {
        System.out.print("Enter Manufacturer ID to reject: ");
        String input = scanner.nextLine().trim();
        try {
            int id = Integer.parseInt(input);
            if (adminService.rejectManufacturer(id)) {
                System.out.println("SUCCESS: Manufacturer ID " + id + " has been REJECTED.");
            } else {
                System.out.println("Failed to reject manufacturer.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Must be an integer.");
        } catch (UserNotFoundException | DatabaseException e) {
            System.err.println("Error rejecting manufacturer: " + e.getMessage());
        }
    }

    private void handleViewPendingSuppliers() {
        try {
            List<Supplier> pending = adminService.viewPendingSuppliers();
            System.out.println("\n----- PENDING SUPPLIERS -----");
            if (pending.isEmpty()) {
                System.out.println("No pending supplier registrations found.");
                return;
            }
            for (Supplier s : pending) {
                System.out.printf("Supplier ID: %d | User ID: %d | Name: %s | Address: %s | Contact: %s | Phone: %s | Status: %s%n",
                        s.getSupplierId(), s.getUserId(), s.getSupplierName(),
                        s.getAddress(), s.getContactNo(), s.getPhoneNo(), s.getStatus());
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading pending suppliers: " + e.getMessage());
        }
    }

    private void handleApproveSupplier() {
        System.out.print("Enter Supplier ID to approve: ");
        String input = scanner.nextLine().trim();
        try {
            int id = Integer.parseInt(input);
            if (adminService.approveSupplier(id)) {
                System.out.println("SUCCESS: Supplier ID " + id + " has been APPROVED.");
            } else {
                System.out.println("Failed to approve supplier.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Must be an integer.");
        } catch (UserNotFoundException | DatabaseException e) {
            System.err.println("Error approving supplier: " + e.getMessage());
        }
    }

    private void handleRejectSupplier() {
        System.out.print("Enter Supplier ID to reject: ");
        String input = scanner.nextLine().trim();
        try {
            int id = Integer.parseInt(input);
            if (adminService.rejectSupplier(id)) {
                System.out.println("SUCCESS: Supplier ID " + id + " has been REJECTED.");
            } else {
                System.out.println("Failed to reject supplier.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Must be an integer.");
        } catch (UserNotFoundException | DatabaseException e) {
            System.err.println("Error rejecting supplier: " + e.getMessage());
        }
    }

    private void handleViewAllUsers() {
        try {
            List<User> users = adminService.viewAllUsers();
            System.out.println("\n----- ALL REGISTERED USERS -----");
            if (users.isEmpty()) {
                System.out.println("No users found in database.");
                return;
            }
            for (User u : users) {
                System.out.printf("User ID: %d | Username: %s | Phone: %s | Role: %s | Status: %s | Created: %s%n",
                        u.getUserId(), u.getUsername(), u.getPhoneNo(), u.getRole(), u.getStatus(), u.getCreatedAt());
            }
        } catch (DatabaseException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private void handleSearchUser() {
        System.out.print("Enter search term (User ID, Username, or Phone Number): ");
        String query = scanner.nextLine().trim();
        try {
            User u = adminService.searchUser(query);
            System.out.println("\n----- USER SEARCH RESULT -----");
            System.out.printf("User ID: %d | Username: %s | Phone: %s | Role: %s | Status: %s | Created: %s%n",
                    u.getUserId(), u.getUsername(), u.getPhoneNo(), u.getRole(), u.getStatus(), u.getCreatedAt());
        } catch (UserNotFoundException | DatabaseException e) {
            System.err.println("Search failed: " + e.getMessage());
        }
    }

    private void handleDeactivateUser() {
        System.out.print("Enter User ID to deactivate: ");
        String input = scanner.nextLine().trim();
        try {
            int id = Integer.parseInt(input);
            if (adminService.deactivateUser(id)) {
                System.out.println("SUCCESS: User ID " + id + " status set to INACTIVE.");
            } else {
                System.out.println("Failed to deactivate user.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Must be an integer.");
        } catch (UserNotFoundException | DatabaseException e) {
            System.err.println("Error deactivating user: " + e.getMessage());
        }
    }

    private void handleViewUserDetails() {
        System.out.print("Enter User ID to view details: ");
        String input = scanner.nextLine().trim();
        try {
            int id = Integer.parseInt(input);
            User u = adminService.viewUserDetails(id);
            System.out.println("\n----- USER DETAILS -----");
            System.out.println(u.toString());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Must be an integer.");
        } catch (UserNotFoundException | DatabaseException e) {
            System.err.println("Error viewing user details: " + e.getMessage());
        }
    }
}
