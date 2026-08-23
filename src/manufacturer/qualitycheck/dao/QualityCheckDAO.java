package manufacturer.qualitycheck.dao;

import database.DBConnection;
import manufacturer.productionorder.model.ProductionOrder;
import manufacturer.qualitycheck.model.QualityCheck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** JDBC operations for the existing quality_checks table and order status transition. */
public class QualityCheckDAO {
    private static final String ORDER_DETAILS =
            "SELECT po.production_order_id, po.request_id, po.manufacturer_id, s.supplier_name, p.product_name, "
                    + "c.category_name, po.quantity, po.priority, po.start_date, po.completion_date, po.status "
                    + "FROM production_orders po "
                    + "INNER JOIN production_requests pr ON po.request_id = pr.request_id "
                    + "INNER JOIN suppliers s ON pr.supplier_id = s.supplier_id "
                    + "INNER JOIN products p ON po.product_id = p.product_id "
                    + "INNER JOIN categories c ON p.category_id = c.category_id ";

    public List<ProductionOrder> findCompletedOrders(int manufacturerId) throws SQLException {
        String sql = ORDER_DETAILS + "WHERE po.manufacturer_id = ? AND po.status = 'COMPLETED' "
                + "ORDER BY po.completion_date, po.production_order_id";
        List<ProductionOrder> orders = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, manufacturerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(mapOrder(resultSet));
                }
            }
        }
        return orders;
    }

    public ProductionOrder findOrderById(Connection connection, int productionOrderId) throws SQLException {
        String sql = ORDER_DETAILS + "WHERE po.production_order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productionOrderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapOrder(resultSet) : null;
            }
        }
    }

    public boolean qualityCheckExists(Connection connection, int productionOrderId) throws SQLException {
        String sql = "SELECT quality_check_id FROM quality_checks WHERE production_order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productionOrderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int insertQualityCheck(Connection connection, int productionOrderId, String result, String remarks)
            throws SQLException {
        String sql = "INSERT INTO quality_checks (production_order_id, result, remarks) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, productionOrderId);
            statement.setString(2, result);
            if (remarks == null || remarks.trim().isEmpty()) {
                statement.setNull(3, Types.VARCHAR);
            } else {
                statement.setString(3, remarks.trim());
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public boolean updateOrderStatus(Connection connection, int productionOrderId, int manufacturerId, String status)
            throws SQLException {
        String sql = "UPDATE production_orders SET status = ? "
                + "WHERE production_order_id = ? AND manufacturer_id = ? AND status = 'COMPLETED'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, productionOrderId);
            statement.setInt(3, manufacturerId);
            return statement.executeUpdate() > 0;
        }
    }

    public List<QualityCheck> findHistoryByManufacturer(int manufacturerId) throws SQLException {
        String sql = "SELECT qc.quality_check_id, qc.production_order_id, s.supplier_name, p.product_name, "
                + "po.quantity, qc.checked_date, qc.result, qc.remarks FROM quality_checks qc "
                + "INNER JOIN production_orders po ON qc.production_order_id = po.production_order_id "
                + "INNER JOIN production_requests pr ON po.request_id = pr.request_id "
                + "INNER JOIN suppliers s ON pr.supplier_id = s.supplier_id "
                + "INNER JOIN products p ON po.product_id = p.product_id "
                + "WHERE po.manufacturer_id = ? ORDER BY qc.checked_date DESC";
        List<QualityCheck> checks = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, manufacturerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    checks.add(new QualityCheck(resultSet.getInt("quality_check_id"),
                            resultSet.getInt("production_order_id"), resultSet.getString("supplier_name"),
                            resultSet.getString("product_name"), resultSet.getInt("quantity"),
                            resultSet.getTimestamp("checked_date"), resultSet.getString("result"),
                            resultSet.getString("remarks")));
                }
            }
        }
        return checks;
    }

    public boolean manufacturerExists(int manufacturerId) throws SQLException {
        String sql = "SELECT manufacturer_id FROM manufacturers WHERE manufacturer_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, manufacturerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private ProductionOrder mapOrder(ResultSet resultSet) throws SQLException {
        return new ProductionOrder(resultSet.getInt("production_order_id"), resultSet.getInt("request_id"),
                resultSet.getInt("manufacturer_id"), resultSet.getString("supplier_name"),
                resultSet.getString("product_name"), resultSet.getString("category_name"),
                resultSet.getInt("quantity"), resultSet.getString("priority"), resultSet.getDate("start_date"),
                resultSet.getDate("completion_date"), resultSet.getString("status"));
    }
}
