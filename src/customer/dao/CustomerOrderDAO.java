package customer.dao;

import customer.model.CustomerOrder;
import customer.model.CustomerOrderDetails;
import customer.model.CustomerOrderSummary;
import customer.model.SupplierProductStockView;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrderDAO {
    public int createOrder(Connection connection, CustomerOrder order) throws SQLException {
        String sql = "INSERT INTO customer_orders (customer_id, supplier_id, status, total_amount) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, order.getCustomerId());
            statement.setInt(2, order.getSupplierId());
            statement.setString(3, order.getStatus());
            statement.setBigDecimal(4, order.getTotalAmount());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);
                    order.setOrderId(orderId);
                    return orderId;
                }
            }
        }

        throw new SQLException("Creating customer order failed. No generated order ID returned.");
    }

    public CustomerOrder getOrderById(int orderId) throws SQLException {
        String sql = "SELECT order_id, customer_id, supplier_id, order_date, total_amount, status "
                + "FROM customer_orders WHERE order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapOrder(resultSet);
                }
            }
        }

        return null;
    }

    public CustomerOrder getOrderByIdForCustomer(int orderId, int customerId) throws SQLException {
        String sql = "SELECT order_id, customer_id, supplier_id, order_date, total_amount, status "
                + "FROM customer_orders WHERE order_id = ? AND customer_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);
            statement.setInt(2, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapOrder(resultSet);
                }
            }
        }

        return null;
    }

    public List<CustomerOrderSummary> getOrdersByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT co.order_id, co.order_date, s.company_name AS supplier_name, "
                + "co.total_amount, co.status "
                + "FROM customer_orders co "
                + "INNER JOIN suppliers s ON co.supplier_id = s.supplier_id "
                + "WHERE co.customer_id = ? "
                + "ORDER BY co.order_date DESC";

        List<CustomerOrderSummary> orders = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(new CustomerOrderSummary(
                            resultSet.getInt("order_id"),
                            resultSet.getTimestamp("order_date"),
                            resultSet.getString("supplier_name"),
                            resultSet.getBigDecimal("total_amount"),
                            resultSet.getString("status")
                    ));
                }
            }
        }

        return orders;
    }

    public CustomerOrderDetails getOrderDetailsForCustomer(int orderId, int customerId) throws SQLException {
        String sql = "SELECT co.order_id, co.order_date, s.company_name AS supplier_name, "
                + "co.total_amount, co.status "
                + "FROM customer_orders co "
                + "INNER JOIN suppliers s ON co.supplier_id = s.supplier_id "
                + "WHERE co.order_id = ? AND co.customer_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);
            statement.setInt(2, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new CustomerOrderDetails(
                            resultSet.getInt("order_id"),
                            resultSet.getTimestamp("order_date"),
                            resultSet.getString("supplier_name"),
                            resultSet.getBigDecimal("total_amount"),
                            resultSet.getString("status")
                    );
                }
            }
        }

        return null;
    }

    public boolean updateOrderStatus(int orderId, int customerId, String status) throws SQLException {
        String sql = "UPDATE customer_orders SET status = ? WHERE order_id = ? AND customer_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, orderId);
            statement.setInt(3, customerId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean cancelOrder(int orderId, int customerId) throws SQLException {
        String sql = "UPDATE customer_orders SET status = 'CANCELLED' "
                + "WHERE order_id = ? AND customer_id = ? AND status = 'PLACED'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);
            statement.setInt(2, customerId);
            return statement.executeUpdate() > 0;
        }
    }

    public void refreshOrderTotal(Connection connection, int orderId) throws SQLException {
        String sql = "UPDATE customer_orders "
                + "SET total_amount = ("
                + "SELECT COALESCE(SUM(subtotal), 0.00) FROM order_items WHERE order_id = ?"
                + ") WHERE order_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    public BigDecimal getOrderTotal(Connection connection, int orderId) throws SQLException {
        String sql = "SELECT total_amount FROM customer_orders WHERE order_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal("total_amount");
                }
            }
        }

        return BigDecimal.ZERO;
    }

    public BigDecimal getActiveProductPrice(Connection connection, int productId) throws SQLException {
        String sql = "SELECT unit_price FROM products WHERE product_id = ? AND status = 'ACTIVE'";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal("unit_price");
                }
            }
        }

        return null;
    }

    public SupplierProductStockView getSupplierStockForProduct(Connection connection, int productId, int supplierId)
            throws SQLException {
        String sql = "SELECT s.supplier_id, s.company_name AS supplier_name, inv.product_id, inv.quantity "
                + "FROM inventory inv "
                + "INNER JOIN suppliers s ON inv.owner_type = 'SUPPLIER' "
                + "AND inv.owner_id = s.supplier_id "
                + "INNER JOIN products p ON inv.product_id = p.product_id "
                + "WHERE inv.product_id = ? "
                + "AND s.supplier_id = ? "
                + "AND p.status = 'ACTIVE'";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setInt(2, supplierId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new SupplierProductStockView(
                            resultSet.getInt("supplier_id"),
                            resultSet.getString("supplier_name"),
                            resultSet.getInt("product_id"),
                            resultSet.getBigDecimal("quantity")
                    );
                }
            }
        }

        return null;
    }

    public List<SupplierProductStockView> getSuppliersForProduct(int productId) throws SQLException {
        String sql = "SELECT s.supplier_id, s.company_name AS supplier_name, inv.product_id, inv.quantity "
                + "FROM inventory inv "
                + "INNER JOIN suppliers s ON inv.owner_type = 'SUPPLIER' "
                + "AND inv.owner_id = s.supplier_id "
                + "INNER JOIN products p ON inv.product_id = p.product_id "
                + "WHERE inv.product_id = ? "
                + "AND p.status = 'ACTIVE' "
                + "AND inv.quantity > 0 "
                + "ORDER BY s.company_name";

        List<SupplierProductStockView> suppliers = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    suppliers.add(new SupplierProductStockView(
                            resultSet.getInt("supplier_id"),
                            resultSet.getString("supplier_name"),
                            resultSet.getInt("product_id"),
                            resultSet.getBigDecimal("quantity")
                    ));
                }
            }
        }

        return suppliers;
    }

    private CustomerOrder mapOrder(ResultSet resultSet) throws SQLException {
        return new CustomerOrder(
                resultSet.getInt("order_id"),
                resultSet.getInt("customer_id"),
                resultSet.getInt("supplier_id"),
                resultSet.getTimestamp("order_date"),
                resultSet.getBigDecimal("total_amount"),
                resultSet.getString("status")
        );
    }
}
