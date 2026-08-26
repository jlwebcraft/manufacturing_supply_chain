package customer.dao;

import customer.model.OrderItem;
import customer.model.OrderItemDetail;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {
    public int createOrderItem(Connection connection, OrderItem item) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, item.getOrderId());
            statement.setInt(2, item.getProductId());
            statement.setInt(3, item.getQuantity());
            statement.setBigDecimal(4, item.getUnitPrice());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int orderItemId = generatedKeys.getInt(1);
                    item.setOrderItemId(orderItemId);
                    return orderItemId;
                }
            }
        }

        throw new SQLException("Creating order item failed. No generated order item ID returned.");
    }

    public List<OrderItem> getItemsByOrderId(int orderId) throws SQLException {
        String sql = "SELECT order_item_id, order_id, product_id, quantity, unit_price "
                + "FROM order_items WHERE order_id = ?";

        List<OrderItem> items = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapOrderItem(resultSet));
                }
            }
        }

        return items;
    }

    public List<OrderItemDetail> getItemDetailsByOrderIdForCustomer(int orderId, int customerId)
            throws SQLException {
        String sql = "SELECT p.product_name, oi.quantity, oi.unit_price, oi.subtotal "
                + "FROM customer_orders co "
                + "INNER JOIN order_items oi ON co.order_id = oi.order_id "
                + "INNER JOIN products p ON oi.product_id = p.product_id "
                + "WHERE co.order_id = ? AND co.customer_id = ? "
                + "ORDER BY oi.order_item_id";

        List<OrderItemDetail> details = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);
            statement.setInt(2, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    details.add(new OrderItemDetail(
                            resultSet.getString("product_name"),
                            resultSet.getInt("quantity"),
                            resultSet.getBigDecimal("unit_price"),
                            resultSet.getBigDecimal("subtotal")
                    ));
                }
            }
        }

        return details;
    }

    public OrderItem getOrderItemById(int orderItemId) throws SQLException {
        String sql = "SELECT order_item_id, order_id, product_id, quantity, unit_price "
                + "FROM order_items WHERE order_item_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderItemId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapOrderItem(resultSet);
                }
            }
        }

        return null;
    }

    public boolean deleteOrderItem(int orderItemId) throws SQLException {
        String sql = "DELETE FROM order_items WHERE order_item_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderItemId);
            return statement.executeUpdate() > 0;
        }
    }

    private OrderItem mapOrderItem(ResultSet resultSet) throws SQLException {
        return new OrderItem(
                resultSet.getInt("order_item_id"),
                resultSet.getInt("order_id"),
                resultSet.getInt("product_id"),
                resultSet.getInt("quantity"),
                resultSet.getBigDecimal("unit_price")
        );
    }
}
