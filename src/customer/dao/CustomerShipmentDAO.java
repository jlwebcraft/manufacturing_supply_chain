package customer.dao;

import customer.model.CustomerShipmentView;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerShipmentDAO {
    private static final String CUSTOMER_SHIPMENT_SELECT =
            "SELECT s.shipment_id, s.customer_order_id AS order_id, s.tracking_number, "
                    + "s.shipment_type, s.status, s.shipped_date, s.delivered_date, "
                    + "sup.company_name AS supplier_name, c.customer_name, c.address AS delivery_address "
                    + "FROM shipments s "
                    + "INNER JOIN customer_orders co ON s.customer_order_id = co.order_id "
                    + "INNER JOIN customers c ON co.customer_id = c.customer_id "
                    + "LEFT JOIN suppliers sup ON s.supplier_id = sup.supplier_id "
                    + "WHERE s.shipment_type = 'SUPPLIER_TO_CUSTOMER' ";

    public List<CustomerShipmentView> getShipmentsByCustomerId(int customerId) throws SQLException {
        String sql = CUSTOMER_SHIPMENT_SELECT
                + "AND co.customer_id = ? "
                + "ORDER BY s.shipped_date DESC, s.shipment_id DESC";

        List<CustomerShipmentView> shipments = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    shipments.add(mapShipment(resultSet));
                }
            }
        }

        return shipments;
    }

    public CustomerShipmentView getShipmentByIdForCustomer(int shipmentId, int customerId) throws SQLException {
        String sql = CUSTOMER_SHIPMENT_SELECT
                + "AND co.customer_id = ? "
                + "AND s.shipment_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);
            statement.setInt(2, shipmentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapShipment(resultSet);
                }
            }
        }

        return null;
    }

    public CustomerShipmentView getShipmentByOrderIdForCustomer(int orderId, int customerId) throws SQLException {
        String sql = CUSTOMER_SHIPMENT_SELECT
                + "AND co.customer_id = ? "
                + "AND co.order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);
            statement.setInt(2, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapShipment(resultSet);
                }
            }
        }

        return null;
    }

    private CustomerShipmentView mapShipment(ResultSet resultSet) throws SQLException {
        return new CustomerShipmentView(
                resultSet.getInt("shipment_id"),
                resultSet.getInt("order_id"),
                resultSet.getString("tracking_number"),
                resultSet.getString("shipment_type"),
                resultSet.getString("status"),
                resultSet.getTimestamp("shipped_date"),
                resultSet.getTimestamp("delivered_date"),
                resultSet.getString("supplier_name"),
                resultSet.getString("customer_name"),
                resultSet.getString("delivery_address")
        );
    }
}
