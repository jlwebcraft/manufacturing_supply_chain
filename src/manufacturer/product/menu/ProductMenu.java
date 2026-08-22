package manufacturer.product.menu;

import manufacturer.product.model.Product;
import manufacturer.product.service.ProductService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/** Terminal controller/menu for Product Management. */
public class ProductMenu {
    private final ProductService productService = new ProductService();
    private final Scanner scanner = new Scanner(System.in);

    /** Displays Product Management when called by ManufacturerMenu. */
    public void showMenu() {
        boolean running = true;
        while (running) {
            printMenu();
            try {
                switch (scanner.nextLine().trim()) {
                    case "1": addProduct(); break;
                    case "2": printProducts(productService.viewProducts()); break;
                    case "3": searchProduct(); break;
                    case "4": viewProductDetails(); break;
                    case "5": updateProduct(); break;
                    case "6": deactivateProduct(); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice. Please choose 0 to 6.");
                }
            } catch (IllegalArgumentException exception) {
                System.out.println("Input error: " + exception.getMessage());
            } catch (SQLException exception) {
                System.out.println("Database error: " + exception.getMessage());
            }
        }
        System.out.println("Leaving Product Management.");
    }

    private void addProduct() throws SQLException {
        Product product = readProductDetails(0);
        int productId = productService.addProduct(product.getManufacturerId(), product.getCategoryId(),
                product.getProductName(), product.getDescription(), product.getPrice());
        System.out.println("Product added successfully. ID: " + productId);
    }

    private void searchProduct() throws SQLException {
        System.out.print("Enter product ID, product name, description, or category name: ");
        printProducts(productService.searchProducts(scanner.nextLine()));
    }

    private void viewProductDetails() throws SQLException {
        Product product = productService.viewProductDetails(readPositiveInt("Product ID"));
        if (product == null) {
            System.out.println("Product ID not found.");
            return;
        }
        System.out.println("\n--- Product Details ---");
        System.out.println("Product ID: " + product.getProductId());
        System.out.println("Manufacturer ID: " + product.getManufacturerId());
        System.out.println("Category: " + product.getCategoryName() + " (ID: " + product.getCategoryId() + ")");
        System.out.println("Product Name: " + product.getProductName());
        System.out.println("Description: " + (product.getDescription() == null ? "" : product.getDescription()));
        System.out.println("Price: " + product.getPrice());
        System.out.println("Status: " + product.getStatus());
    }

    private void updateProduct() throws SQLException {
        int productId = readPositiveInt("Product ID to update");
        Product product = readProductDetails(productId);
        System.out.println(productService.updateProduct(productId, product.getManufacturerId(), product.getCategoryId(),
                product.getProductName(), product.getDescription(), product.getPrice())
                ? "Product updated successfully." : "Product not found for this manufacturer.");
    }

    private void deactivateProduct() throws SQLException {
        int productId = readPositiveInt("Product ID");
        int manufacturerId = readPositiveInt("Manufacturer ID");
        System.out.print("Deactivate this product? (Y/N): ");
        if (!"Y".equalsIgnoreCase(scanner.nextLine().trim())) {
            System.out.println("Deactivation cancelled.");
            return;
        }
        System.out.println(productService.deactivateProduct(productId, manufacturerId)
                ? "Product deactivated successfully." : "Product not found, belongs to another manufacturer, or is already inactive.");
    }

    private Product readProductDetails(int productId) {
        int manufacturerId = readPositiveInt("Manufacturer ID");
        int categoryId = readPositiveInt("Category ID");
        System.out.print("Product name: ");
        String name = scanner.nextLine();
        System.out.print("Description (optional): ");
        String description = scanner.nextLine();
        System.out.print("Price: ");
        BigDecimal price;
        try {
            price = new BigDecimal(scanner.nextLine().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Price must be a valid number.");
        }
        return new Product(productId, manufacturerId, categoryId, name, description, price, null, null);
    }

    private int readPositiveInt(String label) {
        System.out.print(label + ": ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a number.");
        }
    }

    private void printProducts(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        System.out.printf("%-5s %-16s %-22s %-18s %-12s %-10s%n",
                "ID", "Manufacturer ID", "Product", "Category", "Price", "Status");
        System.out.println("------------------------------------------------------------------------------------------------");
        for (Product product : products) {
            System.out.printf("%-5d %-16d %-22s %-18s %-12s %-10s%n", product.getProductId(),
                    product.getManufacturerId(), product.getProductName(), product.getCategoryName(),
                    product.getPrice(), product.getStatus());
        }
    }

    private void printMenu() {
        System.out.println("\n--- Product Management ---");
        System.out.println("1. Add Product");
        System.out.println("2. View Products");
        System.out.println("3. Search Product");
        System.out.println("4. View Product Details");
        System.out.println("5. Update Product");
        System.out.println("6. Deactivate Product");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }
}
