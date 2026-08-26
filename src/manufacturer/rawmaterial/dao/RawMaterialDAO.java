package manufacturer.rawmaterial.dao;

import database.DBConnection;
import manufacturer.rawmaterial.model.RawMaterial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** JDBC CRUD operations for raw_materials, with inventory quantities for display. */
public class RawMaterialDAO {
    private static final String MATERIAL_WITH_INVENTORY =
            "SELECT rm.material_id, rm.manufacturer_id, rm.material_name, rm.unit, rm.minimum_stock, rm.status, "
                    + "COALESCE(SUM(i.quantity), 0) AS inventory_quantity "
                    + "FROM raw_materials rm "
                    + "LEFT JOIN inventory i ON i.material_id = rm.material_id "
                    + "AND i.manufacturer_id = rm.manufacturer_id AND i.owner_type = 'MANUFACTURER' ";

    public int add(RawMaterial material) throws SQLException {
        String sql = "INSERT INTO raw_materials (manufacturer_id, material_name, unit, minimum_stock, status) "
                + "VALUES (?, ?, ?, ?, 'ACTIVE')";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, material.getManufacturerId());
            statement.setString(2, material.getMaterialName());
            statement.setString(3, material.getUnit());
            statement.setInt(4, material.getMinimumStock());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public List<RawMaterial> findByManufacturerAndStatus(int manufacturerId, String status) throws SQLException {
        String sql = MATERIAL_WITH_INVENTORY
                + "WHERE rm.manufacturer_id = ? AND rm.status = ? "
                + "GROUP BY rm.material_id, rm.manufacturer_id, rm.material_name, rm.unit, rm.minimum_stock, rm.status "
                + "ORDER BY rm.material_id";
        List<RawMaterial> materials = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, manufacturerId);
            statement.setString(2, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    materials.add(mapMaterial(resultSet));
                }
            }
        }
        return materials;
    }

    public List<RawMaterial> search(int manufacturerId, String keyword) throws SQLException {
        String sql = MATERIAL_WITH_INVENTORY
                + "WHERE rm.manufacturer_id = ? AND (rm.material_name LIKE ? OR rm.unit LIKE ? "
                + "OR CAST(rm.material_id AS CHAR) LIKE ?) "
                + "GROUP BY rm.material_id, rm.manufacturer_id, rm.material_name, rm.unit, rm.minimum_stock, rm.status "
                + "ORDER BY rm.material_id";
        List<RawMaterial> materials = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String value = "%" + keyword + "%";
            statement.setInt(1, manufacturerId);
            statement.setString(2, value);
            statement.setString(3, value);
            statement.setString(4, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    materials.add(mapMaterial(resultSet));
                }
            }
        }
        return materials;
    }

    public RawMaterial findById(int materialId) throws SQLException {
        String sql = MATERIAL_WITH_INVENTORY
                + "WHERE rm.material_id = ? "
                + "GROUP BY rm.material_id, rm.manufacturer_id, rm.material_name, rm.unit, rm.minimum_stock, rm.status";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, materialId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapMaterial(resultSet) : null;
            }
        }
    }

    public boolean update(RawMaterial material) throws SQLException {
        String sql = "UPDATE raw_materials SET material_name = ?, unit = ?, minimum_stock = ? "
                + "WHERE material_id = ? AND manufacturer_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, material.getMaterialName());
            statement.setString(2, material.getUnit());
            statement.setInt(3, material.getMinimumStock());
            statement.setInt(4, material.getMaterialId());
            statement.setInt(5, material.getManufacturerId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deactivate(int materialId, int manufacturerId) throws SQLException {
        String sql = "UPDATE raw_materials SET status = 'INACTIVE' "
                + "WHERE material_id = ? AND manufacturer_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, materialId);
            statement.setInt(2, manufacturerId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean activate(int materialId, int manufacturerId) throws SQLException {
        String sql = "UPDATE raw_materials SET status = 'ACTIVE' "
                + "WHERE material_id = ? AND manufacturer_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, materialId);
            statement.setInt(2, manufacturerId);
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

    private RawMaterial mapMaterial(ResultSet resultSet) throws SQLException {
        return new RawMaterial(resultSet.getInt("material_id"),
                resultSet.getInt("manufacturer_id"), resultSet.getString("material_name"),
                resultSet.getString("unit"), resultSet.getInt("minimum_stock"),
                resultSet.getString("status"), resultSet.getInt("inventory_quantity"));
    }
}
