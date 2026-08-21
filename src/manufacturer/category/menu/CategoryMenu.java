package manufacturer.category.menu;

import manufacturer.category.model.Category;
import manufacturer.category.service.CategoryService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/** Terminal controller/menu for Category Management. */
public class CategoryMenu {
    private final CategoryService categoryService = new CategoryService();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new CategoryMenu().start();
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": addCategory(); break;
                    case "2": viewAllCategories(); break;
                    case "3": searchCategory(); break;
                    case "4": updateCategory(); break;
                    case "5": deactivateCategory(); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice. Please choose 0 to 5.");
                }
            } catch (IllegalArgumentException exception) {
                System.out.println("Input error: " + exception.getMessage());
            } catch (SQLException exception) {
                System.out.println("Database error: " + exception.getMessage());
            }
        }
        System.out.println("Leaving Category Management.");
    }

    private void addCategory() throws SQLException {
        System.out.print("Category name: ");
        String name = scanner.nextLine();
        System.out.print("Description (optional): ");
        String description = scanner.nextLine();
        int id = categoryService.addCategory(name, description);
        System.out.println("Category added successfully. ID: " + id);
    }

    private void viewAllCategories() throws SQLException {
        printCategories(categoryService.viewAllCategories());
    }

    private void searchCategory() throws SQLException {
        System.out.print("Enter category ID, name, or description: ");
        printCategories(categoryService.searchCategories(scanner.nextLine()));
    }

    private void updateCategory() throws SQLException {
        int id = readCategoryId();
        System.out.print("New category name: ");
        String name = scanner.nextLine();
        System.out.print("New description (leave blank to clear): ");
        String description = scanner.nextLine();
        System.out.println(categoryService.updateCategory(id, name, description)
                ? "Category updated successfully." : "Category ID not found.");
    }

    private void deactivateCategory() throws SQLException {
        int id = readCategoryId();
        System.out.print("Deactivate this category? (Y/N): ");
        if (!"Y".equalsIgnoreCase(scanner.nextLine().trim())) {
            System.out.println("Deactivation cancelled.");
            return;
        }
        System.out.println(categoryService.deactivateCategory(id)
                ? "Category deactivated successfully." : "Category ID not found or already inactive.");
    }

    private int readCategoryId() {
        System.out.print("Category ID: ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Category ID must be a number.");
        }
    }

    private void printCategories(List<Category> categories) {
        if (categories.isEmpty()) {
            System.out.println("No categories found.");
            return;
        }
        System.out.printf("%-5s %-25s %-40s %-10s%n", "ID", "Name", "Description", "Status");
        System.out.println("--------------------------------------------------------------------------------");
        for (Category category : categories) {
            String description = category.getDescription() == null ? "" : category.getDescription();
            System.out.printf("%-5d %-25s %-40s %-10s%n", category.getCategoryId(),
                    category.getCategoryName(), description, category.getStatus());
        }
    }

    private void printMenu() {
        System.out.println("\n--- Category Management ---");
        System.out.println("1. Add Category");
        System.out.println("2. View All Categories");
        System.out.println("3. Search Category");
        System.out.println("4. Update Category");
        System.out.println("5. Deactivate Category");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }
}
