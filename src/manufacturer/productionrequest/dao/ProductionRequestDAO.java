package manufacturer.productionrequest.dao;

import database.DBConnection;
import manufacturer.productionrequest.model.ProductionRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** JDBC access for Manufacturer-side processing of existing production_requests. */
public class ProductionRequestDAO {
    private static final String REQUEST_DETAILS =
            "SELECT pr.request_id, pr.supplier_id, pr.manufacturer_id, pr.product_id, s.supplier_name, "
                    + "p.product_name, c.category_name, pr.quantity, pr.priority, pr.required_date, "
                    + "pr.request_date, pr.status FROM production_requests pr "
                    + "INNER JOIN suppliers s ON pr.supplier_id = s.supplier_id "
                    + "INNER JOIN products p ON pr.product_id = p.product_id "
                    + "INNER JOIN categories c ON p.category_id = c.category_id ";

    public List<ProductionRequest> findPendingByManufacturer(int manufacturerId) throws SQLException {
        return getRequests(REQUEST_DETAILS + "WHERE pr.manufacturer_id = ? AND pr.status = 'PENDING' "
                + "ORDER BY pr.priority DESC, pr.required_date, pr.request_date", manufacturerId);
    }

    public List<ProductionRequest> findHistoryByManufacturer(int manufacturerId) throws SQLException {
        return getRequests(REQUEST_DETAILS + "WHERE pr.manufacturer_id = ? AND pr.status IN ('APPROVED', 'REJECTED') "
                + "ORDER BY pr.request_date DESC", manufacturerId);
    }

    public ProductionRequest findById(int requestId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return findById(connection, requestId);
        }
    }

    public ProductionRequest findById(Connection connection, int requestId) throws SQLException {
        String sql = REQUEST_DETAILS + "WHERE pr.request_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRequest(resultSet) : null;
            }
        }
    }

    public boolean updatePendingStatus(Connection connection, int requestId, int manufacturerId, String status)
            throws SQLException {
        String sql = "UPDATE production_requests SET status = ? "
                + "WHERE request_id = ? AND manufacturer_id = ? AND status = 'PENDING'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, requestId);
            statement.setInt(3, manufacturerId);
            return statement.executeUpdate() > 0;
        }
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

    private List<ProductionRequest> getRequests(String sql, int manufacturerId) throws SQLException {
        List<ProductionRequest> requests = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, manufacturerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(mapRequest(resultSet));
                }
            }
        }
        return requests;
    }

    private ProductionRequest mapRequest(ResultSet resultSet) throws SQLException {
        return new ProductionRequest(resultSet.getInt("request_id"), resultSet.getInt("supplier_id"),
                resultSet.getInt("manufacturer_id"), resultSet.getInt("product_id"),
                resultSet.getString("supplier_name"), resultSet.getString("product_name"),
                resultSet.getString("category_name"), resultSet.getInt("quantity"),
                resultSet.getString("priority"), resultSet.getDate("required_date"),
                resultSet.getTimestamp("request_date"), resultSet.getString("status"));
    }
}
