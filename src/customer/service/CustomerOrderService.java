package customer.service;

import customer.dao.CustomerOrderDAO;
import customer.dao.CustomerProductDAO;
import customer.dao.OrderItemDAO;
import customer.model.Customer;
import customer.model.CustomerOrder;
import customer.model.CustomerOrderDetails;
import customer.model.CustomerOrderSummary;
import customer.model.CustomerProductView;
import customer.model.OrderItem;
import customer.model.OrderItemDetail;
import customer.model.OrderItemRequest;
import customer.model.OrderPlacementResult;
import customer.model.SupplierProductStockView;
import exception.CustomerNotFoundException;
import exception.InsufficientStockException;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrderService {
    private static final String STATUS_PLACED = "PLACED";

    private final CustomerOrderDAO customerOrderDAO;
    private final OrderItemDAO orderItemDAO;
    private final CustomerProductDAO customerProductDAO;

    public CustomerOrderService() {
        this(new CustomerOrderDAO(), new OrderItemDAO(), new CustomerProductDAO());
    }

    public CustomerOrderService(CustomerOrderDAO customerOrderDAO, OrderItemDAO orderItemDAO,
                                CustomerProductDAO customerProductDAO) {
        this.customerOrderDAO = customerOrderDAO;
        this.orderItemDAO = orderItemDAO;
        this.customerProductDAO = customerProductDAO;
    }

    public OrderPlacementResult placeSingleProductOrder(Customer customer, int productId, int supplierId, int quantity)
            throws SQLException, CustomerNotFoundException, InsufficientStockException {

        List<OrderItemRequest> items = new ArrayList<>();
        items.add(new OrderItemRequest(productId, quantity));
        return placeOrder(customer, supplierId, items);
    }

    public OrderPlacementResult placeOrder(Customer customer, int supplierId, List<OrderItemRequest> items)
            throws SQLException, CustomerNotFoundException, InsufficientStockException {

        validateCustomer(customer);
        validateSupplierId(supplierId);
        validateItems(items);

        Connection connection = null;
        boolean originalAutoCommit = true;

        try {
            connection = DBConnection.getConnection();
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            CustomerOrder order = new CustomerOrder(
                    customer.getCustomerId(),
                    supplierId,
                    BigDecimal.ZERO,
                    STATUS_PLACED
            );
            int orderId = customerOrderDAO.createOrder(connection, order);

            BigDecimal expectedTotal = BigDecimal.ZERO;

            for (OrderItemRequest request : items) {
                BigDecimal unitPrice = customerOrderDAO.getActiveProductPrice(connection, request.getProductId());
                if (unitPrice == null) {
                    throw new IllegalArgumentException("Invalid or inactive product ID: " + request.getProductId());
                }

                SupplierProductStockView stock = customerOrderDAO.getSupplierStockForProduct(
                        connection,
                        request.getProductId(),
                        supplierId
                );
                validateStock(request, stock);

                OrderItem orderItem = new OrderItem(orderId, request.getProductId(), request.getQuantity(), unitPrice);
                orderItemDAO.createOrderItem(connection, orderItem);

                expectedTotal = expectedTotal.add(unitPrice.multiply(BigDecimal.valueOf(request.getQuantity())));
            }

            BigDecimal databaseTotal = customerOrderDAO.getOrderTotal(connection, orderId);
            connection.commit();

            return new OrderPlacementResult(orderId, expectedTotal, databaseTotal, STATUS_PLACED);
        } catch (SQLException | RuntimeException | InsufficientStockException exception) {
            rollbackQuietly(connection);
            throw exception;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(originalAutoCommit);
                } finally {
                    connection.close();
                }
            }
        }
    }

    public List<SupplierProductStockView> getSuppliersForProduct(int productId) throws SQLException {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number.");
        }

        return customerOrderDAO.getSuppliersForProduct(productId);
    }

    public List<CustomerOrderSummary> getOrderHistory(Customer customer)
            throws SQLException, CustomerNotFoundException {
        validateCustomer(customer);
        return customerOrderDAO.getOrdersByCustomerId(customer.getCustomerId());
    }

    public CustomerOrderDetails getOrderDetails(Customer customer, int orderId)
            throws SQLException, CustomerNotFoundException {
        validateCustomer(customer);
        validateOrderId(orderId);

        CustomerOrderDetails details = customerOrderDAO.getOrderDetailsForCustomer(orderId, customer.getCustomerId());
        if (details == null) {
            throw new CustomerNotFoundException("Order not found for the logged-in customer.");
        }

        List<OrderItemDetail> items = orderItemDAO.getItemDetailsByOrderIdForCustomer(
                orderId,
                customer.getCustomerId()
        );
        details.setItems(items);
        return details;
    }

    public boolean cancelOrder(Customer customer, int orderId)
            throws SQLException, CustomerNotFoundException {
        validateCustomer(customer);
        validateOrderId(orderId);

        CustomerOrder order = customerOrderDAO.getOrderByIdForCustomer(orderId, customer.getCustomerId());
        if (order == null) {
            throw new CustomerNotFoundException("Order not found for the logged-in customer.");
        }

        if (!STATUS_PLACED.equals(order.getStatus())) {
            return false;
        }

        return customerOrderDAO.cancelOrder(orderId, customer.getCustomerId());
    }

    public CustomerProductView getProductForOrder(int productId) throws SQLException {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number.");
        }

        return customerProductDAO.getActiveProductDetailsById(productId);
    }

    private void validateCustomer(Customer customer) throws CustomerNotFoundException {
        if (customer == null || customer.getCustomerId() <= 0) {
            throw new CustomerNotFoundException("Valid logged-in customer is required.");
        }
    }

    private void validateSupplierId(int supplierId) {
        if (supplierId <= 0) {
            throw new IllegalArgumentException("Supplier ID must be a positive number.");
        }
    }

    private void validateOrderId(int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number.");
        }
    }

    private void validateItems(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("At least one order item is required.");
        }

        for (OrderItemRequest item : items) {
            if (item == null || item.getProductId() <= 0) {
                throw new IllegalArgumentException("Product ID must be a positive number.");
            }

            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero.");
            }
        }
    }

    private void validateStock(OrderItemRequest request, SupplierProductStockView stock)
            throws InsufficientStockException {
        if (stock == null) {
            throw new InsufficientStockException("Selected supplier does not have this product in inventory.");
        }

        BigDecimal requestedQuantity = BigDecimal.valueOf(request.getQuantity());
        if (stock.getAvailableQuantity().compareTo(requestedQuantity) < 0) {
            throw new InsufficientStockException(
                    "Insufficient stock. Available: " + stock.getAvailableQuantity()
                            + ", requested: " + request.getQuantity()
            );
        }
    }

    private void rollbackQuietly(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }
}
