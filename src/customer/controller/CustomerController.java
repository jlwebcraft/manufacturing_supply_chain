package customer.controller;

import customer.model.Customer;
import customer.model.CustomerOrderDetails;
import customer.model.CustomerOrderSummary;
import customer.model.CustomerProductView;
import customer.model.CustomerProfileView;
import customer.model.CustomerShipmentView;
import customer.model.OrderItemDetail;
import customer.model.OrderPlacementResult;
import customer.model.SupplierProductStockView;
import customer.service.CustomerOrderService;
import customer.service.CustomerShipmentService;
import customer.service.CustomerService;
import exception.CustomerNotFoundException;
import exception.InsufficientStockException;
import exception.InvalidLoginException;
import util.InputUtil;

import java.sql.SQLException;
import java.util.List;

public class CustomerController {
    private final CustomerService customerService;
    private final CustomerOrderService customerOrderService;
    private final CustomerShipmentService customerShipmentService;
    private final InputUtil inputUtil;

    public CustomerController() {
        this(new CustomerService(), new CustomerOrderService(), new CustomerShipmentService(), InputUtil.getInstance());
    }

    public CustomerController(CustomerService customerService, InputUtil inputUtil) {
        this(customerService, new CustomerOrderService(), new CustomerShipmentService(), inputUtil);
    }

    public CustomerController(CustomerService customerService, CustomerOrderService customerOrderService,
                              InputUtil inputUtil) {
        this(customerService, customerOrderService, new CustomerShipmentService(), inputUtil);
    }

    public CustomerController(CustomerService customerService, CustomerOrderService customerOrderService,
                              CustomerShipmentService customerShipmentService, InputUtil inputUtil) {
        this.customerService = customerService;
        this.customerOrderService = customerOrderService;
        this.customerShipmentService = customerShipmentService;
        this.inputUtil = inputUtil;
    }

    public void runCustomerLogin() {
        System.out.println("CUSTOMER LOGIN");
        String phoneNo = inputUtil.readLine("Phone Number: ");
        String pin = inputUtil.readLine("PIN: ");

        try {
            Customer customer = customerService.login(phoneNo, pin);
            System.out.println("Login successful. Welcome, " + customer.getCustomerName() + ".");
            runCustomerMenu(customer);
        } catch (InvalidLoginException exception) {
            System.out.println(exception.getMessage());
        } catch (SQLException exception) {
            System.out.println("Database error during customer login: " + exception.getMessage());
        }
    }

    public void runCustomerMenu(Customer customer) {
        if (customer == null) {
            System.out.println("Customer login is required before opening the customer menu.");
            return;
        }

        boolean running = true;
        while (running) {
            printMenu();
            int choice = inputUtil.readInt("Enter choice: ");

            switch (choice) {
                case 1:
                    viewProducts();
                    break;
                case 2:
                    searchProducts();
                    break;
                case 3:
                    viewProductDetails();
                    break;
                case 4:
                    placeOrder(customer);
                    break;
                case 5:
                    viewMyOrders(customer);
                    break;
                case 6:
                    viewOrderDetails(customer);
                    break;
                case 7:
                    cancelOrder(customer);
                    break;
                case 8:
                    trackShipment(customer);
                    break;
                case 9:
                    viewProfile(customer);
                    break;
                case 10:
                    System.out.println("Logged out from customer menu.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 10.");
                    break;
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=================================");
        System.out.println("CUSTOMER MENU");
        System.out.println("=============");
        System.out.println("1. View Products");
        System.out.println("2. Search Products");
        System.out.println("3. View Product Details");
        System.out.println("4. Place Order");
        System.out.println("5. View My Orders");
        System.out.println("6. View Order Details");
        System.out.println("7. Cancel Order");
        System.out.println("8. Track Shipment");
        System.out.println("9. View Profile");
        System.out.println("10. Logout");
    }

    private void viewProducts() {
        try {
            List<CustomerProductView> products = customerService.viewProducts();
            if (products.isEmpty()) {
                System.out.println("No active products are available.");
                return;
            }

            printProductList(products);
        } catch (SQLException exception) {
            System.out.println("Database error while loading products: " + exception.getMessage());
        }
    }

    private void searchProducts() {
        String searchText = inputUtil.readLine("Enter product name to search: ");

        try {
            List<CustomerProductView> products = customerService.searchProducts(searchText);
            if (products.isEmpty()) {
                System.out.println("No active products matched your search.");
                return;
            }

            printProductList(products);
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        } catch (SQLException exception) {
            System.out.println("Database error while searching products: " + exception.getMessage());
        }
    }

    private void viewProductDetails() {
        int productId = inputUtil.readInt("Enter Product ID: ");

        try {
            CustomerProductView product = customerService.getProductDetails(productId);
            if (product == null) {
                System.out.println("Product not found.");
                return;
            }

            printProductDetails(product);
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        } catch (SQLException exception) {
            System.out.println("Database error while loading product details: " + exception.getMessage());
        }
    }

    private void placeOrder(Customer customer) {
        try {
            List<CustomerProductView> products = customerService.viewProducts();
            if (products.isEmpty()) {
                System.out.println("No active products are available.");
                return;
            }

            printProductList(products);

            int productId = inputUtil.readInt("Enter Product ID to order: ");
            CustomerProductView product = customerOrderService.getProductForOrder(productId);
            if (product == null) {
                System.out.println("Invalid or inactive product.");
                return;
            }

            List<SupplierProductStockView> suppliers = customerOrderService.getSuppliersForProduct(productId);
            if (suppliers.isEmpty()) {
                System.out.println("No supplier currently has stock for this product.");
                return;
            }

            printSupplierChoices(suppliers);

            int supplierId = inputUtil.readInt("Enter Supplier ID: ");
            int quantity = inputUtil.readInt("Enter Quantity: ");

            OrderPlacementResult result = customerOrderService.placeSingleProductOrder(
                    customer,
                    productId,
                    supplierId,
                    quantity
            );

            System.out.println();
            System.out.println("ORDER CONFIRMATION");
            System.out.println("Order ID: " + result.getOrderId());
            System.out.println("Status: " + result.getStatus());
            System.out.println("Expected Total: " + result.getExpectedTotal());
            System.out.println("Database Total: " + result.getDatabaseTotal());
            System.out.println("Inventory is not deducted until the Supplier creates a Sale.");
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        } catch (InsufficientStockException exception) {
            System.out.println(exception.getMessage());
        } catch (CustomerNotFoundException exception) {
            System.out.println(exception.getMessage());
        } catch (SQLException exception) {
            System.out.println("Database error while placing order: " + exception.getMessage());
        }
    }

    private void viewMyOrders(Customer customer) {
        try {
            List<CustomerOrderSummary> orders = customerOrderService.getOrderHistory(customer);
            if (orders.isEmpty()) {
                System.out.println("No customer orders found.");
                return;
            }

            System.out.printf("%-10s %-22s %-25s %-14s %-12s%n",
                    "Order ID", "Order Date", "Supplier", "Total", "Status");

            for (CustomerOrderSummary order : orders) {
                System.out.printf("%-10d %-22s %-25s %-14s %-12s%n",
                        order.getOrderId(),
                        order.getOrderDate(),
                        order.getSupplierName(),
                        order.getTotalAmount(),
                        order.getStatus());
            }
        } catch (CustomerNotFoundException exception) {
            System.out.println(exception.getMessage());
        } catch (SQLException exception) {
            System.out.println("Database error while loading order history: " + exception.getMessage());
        }
    }

    private void viewOrderDetails(Customer customer) {
        int orderId = inputUtil.readInt("Enter Order ID: ");

        try {
            CustomerOrderDetails details = customerOrderService.getOrderDetails(customer, orderId);
            System.out.println();
            System.out.println("ORDER DETAILS");
            System.out.println("Order ID: " + details.getOrderId());
            System.out.println("Order Date: " + details.getOrderDate());
            System.out.println("Supplier: " + details.getSupplierName());
            System.out.println("Total: " + details.getTotalAmount());
            System.out.println("Status: " + details.getStatus());

            if (details.getItems().isEmpty()) {
                System.out.println("No items found for this order.");
                return;
            }

            System.out.printf("%-30s %-10s %-12s %-12s%n", "Product", "Quantity", "Unit Price", "Subtotal");
            for (OrderItemDetail item : details.getItems()) {
                System.out.printf("%-30s %-10d %-12s %-12s%n",
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal());
            }
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        } catch (CustomerNotFoundException exception) {
            System.out.println(exception.getMessage());
        } catch (SQLException exception) {
            System.out.println("Database error while loading order details: " + exception.getMessage());
        }
    }

    private void cancelOrder(Customer customer) {
        int orderId = inputUtil.readInt("Enter Order ID to cancel: ");

        try {
            boolean cancelled = customerOrderService.cancelOrder(customer, orderId);
            if (cancelled) {
                System.out.println("Order cancelled successfully.");
            } else {
                System.out.println("Only PLACED orders can be cancelled.");
            }
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        } catch (CustomerNotFoundException exception) {
            System.out.println(exception.getMessage());
        } catch (SQLException exception) {
            System.out.println("Database error while cancelling order: " + exception.getMessage());
        }
    }

    private void trackShipment(Customer customer) {
        try {
            List<CustomerShipmentView> shipments = customerShipmentService.getShipments(customer);
            printShipmentList(shipments);
            if (shipments.isEmpty()) {
                return;
            }

            System.out.println();
            System.out.println("1. Track by Order ID");
            System.out.println("2. Track by Shipment ID");
            System.out.println("3. Back to Customer Menu");

            int choice = inputUtil.readInt("Enter choice: ");
            switch (choice) {
                case 1:
                    trackShipmentByOrder(customer);
                    break;
                case 2:
                    trackShipmentByShipmentId(customer);
                    break;
                case 3:
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 3.");
                    break;
            }
        } catch (CustomerNotFoundException exception) {
            System.out.println(exception.getMessage());
        } catch (SQLException exception) {
            System.out.println("Database error while loading shipments: " + exception.getMessage());
        }
    }

    private void trackShipmentByOrder(Customer customer) {
        int orderId = inputUtil.readInt("Enter Order ID: ");

        try {
            CustomerShipmentView shipment = customerShipmentService.getShipmentByOrderId(customer, orderId);
            if (shipment == null) {
                System.out.println("No shipment has been created for this order yet.");
                return;
            }

            printShipmentDetails(shipment);
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        } catch (CustomerNotFoundException exception) {
            System.out.println(exception.getMessage());
        } catch (SQLException exception) {
            System.out.println("Database error while tracking shipment: " + exception.getMessage());
        }
    }

    private void trackShipmentByShipmentId(Customer customer) {
        int shipmentId = inputUtil.readInt("Enter Shipment ID: ");

        try {
            CustomerShipmentView shipment = customerShipmentService.getShipmentByShipmentId(customer, shipmentId);
            if (shipment == null) {
                System.out.println("Shipment is unavailable for the logged-in customer.");
                return;
            }

            printShipmentDetails(shipment);
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        } catch (CustomerNotFoundException exception) {
            System.out.println(exception.getMessage());
        } catch (SQLException exception) {
            System.out.println("Database error while tracking shipment: " + exception.getMessage());
        }
    }

    private void viewProfile(Customer customer) {
        try {
            CustomerProfileView profile = customerService.getCustomerProfile(customer);
            System.out.println();
            System.out.println("CUSTOMER PROFILE");
            System.out.println("Customer ID: " + profile.getCustomerId());
            System.out.println("Customer Name: " + profile.getCustomerName());
            System.out.println("Address: " + profile.getAddress());
            System.out.println("Phone Number: " + profile.getPhoneNo());
        } catch (CustomerNotFoundException exception) {
            System.out.println(exception.getMessage());
        } catch (SQLException exception) {
            System.out.println("Database error while loading profile: " + exception.getMessage());
        }
    }

    private void printProductList(List<CustomerProductView> products) {
        System.out.printf("%-10s %-30s %-20s %-12s%n",
                "Product ID", "Product Name", "Category", "Price");

        for (CustomerProductView product : products) {
            System.out.printf("%-10d %-30s %-20s %-12s%n",
                    product.getProductId(),
                    product.getProductName(),
                    product.getCategoryName(),
                    product.getUnitPrice());
        }
    }

    private void printProductDetails(CustomerProductView product) {
        System.out.println();
        System.out.println("PRODUCT DETAILS");
        System.out.println("Product ID: " + product.getProductId());
        System.out.println("Product Name: " + product.getProductName());
        System.out.println("Category: " + product.getCategoryName());
        System.out.println("Description: " + product.getDescription());
        System.out.println("Price: " + product.getUnitPrice());
        System.out.println("Manufacturer: " + product.getManufacturerName());
    }

    private void printSupplierChoices(List<SupplierProductStockView> suppliers) {
        System.out.printf("%-12s %-30s %-18s%n", "Supplier ID", "Supplier", "Available Stock");

        for (SupplierProductStockView supplier : suppliers) {
            System.out.printf("%-12d %-30s %-18s%n",
                    supplier.getSupplierId(),
                    supplier.getSupplierName(),
                    supplier.getAvailableQuantity());
        }
    }

    private void printShipmentList(List<CustomerShipmentView> shipments) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("MY SHIPMENTS");
        System.out.println("============");

        if (shipments.isEmpty()) {
            System.out.println("No shipments found for your orders.");
            return;
        }

        System.out.printf("%-12s %-10s %-20s %-22s %-22s %-14s%n",
                "Shipment ID", "Order ID", "Tracking Number", "Shipment Date", "Delivery Date", "Status");

        for (CustomerShipmentView shipment : shipments) {
            System.out.printf("%-12d %-10d %-20s %-22s %-22s %-14s%n",
                    shipment.getShipmentId(),
                    shipment.getOrderId(),
                    shipment.getTrackingNumber(),
                    shipment.getShippedDate(),
                    shipment.getDeliveredDate(),
                    shipment.getStatus());
        }
    }

    private void printShipmentDetails(CustomerShipmentView shipment) {
        System.out.println();
        System.out.println("SHIPMENT DETAILS");
        System.out.println("Shipment ID: " + shipment.getShipmentId());
        System.out.println("Order ID: " + shipment.getOrderId());
        System.out.println("Tracking Number: " + shipment.getTrackingNumber());
        System.out.println("Shipment Type: " + shipment.getShipmentType());
        System.out.println("Status: " + shipment.getStatus());
        System.out.println("Shipment Date: " + shipment.getShippedDate());
        System.out.println("Delivery Date: " + shipment.getDeliveredDate());
        System.out.println("Supplier: " + shipment.getSupplierName());
        System.out.println("Customer: " + shipment.getCustomerName());
        System.out.println("Delivery Address: " + shipment.getDeliveryAddress());
    }
}
