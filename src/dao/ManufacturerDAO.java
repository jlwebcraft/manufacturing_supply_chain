package dao;

import exception.DatabaseException;
import interfaces.CRUDOperations;
import model.Manufacturer;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Manufacturer entity.
 * Handles database operations for the 'manufacturers' table and joins with 'users'.
 */
public class ManufacturerDAO implements CRUDOperations<Manufacturer> {

    @Override
    public boolean add(Manufacturer manufacturer) throws DatabaseException {
        Connection conn = DBConnection.getConnection();
        try {
            return addManufacturer(manufacturer, conn) > 0;
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    /**
     * Inserts a manufacturer record using an existing transaction connection.
     * @param manufacturer Manufacturer object
     * @param conn Connection object managed by transaction
     * @return Generated manufacturer_id
     * @throws DatabaseException on SQL failure
     */
    public int addManufacturer(Manufacturer manufacturer, Connection conn) throws DatabaseException {
        String sql = "INSERT INTO manufacturers (user_id, manufacturer_name, address, contact_no) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, manufacturer.getUserId());
            pstmt.setString(2, manufacturer.getManufacturerName());
            pstmt.setString(3, manufacturer.getAddress());
            pstmt.setString(4, manufacturer.getContactNo());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating manufacturer failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    manufacturer.setManufacturerId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Creating manufacturer failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting manufacturer into database: " + e.getMessage(), e);
        }
    }

    @Override
    public Manufacturer getById(int manufacturerId) throws DatabaseException {
        String sql = "SELECT m.manufacturer_id, m.user_id, m.manufacturer_name, m.address, m.contact_no, " +
                "u.username, u.password, u.phone_no, u.pin, u.role, u.status, u.created_at " +
                "FROM manufacturers m JOIN users u ON m.user_id = u.user_id " +
                "WHERE m.manufacturer_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, manufacturerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToManufacturer(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving manufacturer by ID: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    public Manufacturer getManufacturerByUserId(int userId) throws DatabaseException {
        String sql = "SELECT m.manufacturer_id, m.user_id, m.manufacturer_name, m.address, m.contact_no, " +
                "u.username, u.password, u.phone_no, u.pin, u.role, u.status, u.created_at " +
                "FROM manufacturers m JOIN users u ON m.user_id = u.user_id " +
                "WHERE m.user_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToManufacturer(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving manufacturer by user ID: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    @Override
    public List<Manufacturer> getAll() throws DatabaseException {
        List<Manufacturer> manufacturerList = new ArrayList<>();
        String sql = "SELECT m.manufacturer_id, m.user_id, m.manufacturer_name, m.address, m.contact_no, " +
                "u.username, u.password, u.phone_no, u.pin, u.role, u.status, u.created_at " +
                "FROM manufacturers m JOIN users u ON m.user_id = u.user_id " +
                "ORDER BY m.manufacturer_id ASC";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                manufacturerList.add(mapResultSetToManufacturer(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all manufacturers: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return manufacturerList;
    }

    public List<Manufacturer> getPendingManufacturers() throws DatabaseException {
        List<Manufacturer> pendingList = new ArrayList<>();
        String sql = "SELECT m.manufacturer_id, m.user_id, m.manufacturer_name, m.address, m.contact_no, " +
                "u.username, u.password, u.phone_no, u.pin, u.role, u.status, u.created_at " +
                "FROM manufacturers m JOIN users u ON m.user_id = u.user_id " +
                "WHERE u.status = 'PENDING' ORDER BY m.manufacturer_id ASC";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                pendingList.add(mapResultSetToManufacturer(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving pending manufacturers: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return pendingList;
    }

    @Override
    public boolean update(Manufacturer manufacturer) throws DatabaseException {
        String sql = "UPDATE manufacturers SET manufacturer_name = ?, address = ?, contact_no = ? WHERE manufacturer_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, manufacturer.getManufacturerName());
            pstmt.setString(2, manufacturer.getAddress());
            pstmt.setString(3, manufacturer.getContactNo());
            pstmt.setInt(4, manufacturer.getManufacturerId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating manufacturer: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    @Override
    public boolean delete(int manufacturerId) throws DatabaseException {
        String sql = "DELETE FROM manufacturers WHERE manufacturer_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, manufacturerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting manufacturer: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    private Manufacturer mapResultSetToManufacturer(ResultSet rs) throws SQLException {
        return new Manufacturer(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("phone_no"),
                rs.getString("pin"),
                rs.getString("status"),
                rs.getTimestamp("created_at"),
                rs.getInt("manufacturer_id"),
                rs.getString("manufacturer_name"),
                rs.getString("address"),
                rs.getString("contact_no")
        );
    }
}
