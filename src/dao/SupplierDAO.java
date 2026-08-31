package dao;

import exception.DatabaseException;
import interfaces.CRUDOperations;
import model.Supplier;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Supplier entity.
 * Handles database operations for the 'suppliers' table and joins with 'users'.
 */
public class SupplierDAO implements CRUDOperations<Supplier> {

    @Override
    public boolean add(Supplier supplier) throws DatabaseException {
        Connection conn = DBConnection.getConnection();
        try {
            return addSupplier(supplier, conn) > 0;
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    /**
     * Inserts a supplier record using an existing transaction connection.
     * @param supplier Supplier object
     * @param conn Connection object managed by transaction
     * @return Generated supplier_id
     * @throws DatabaseException on SQL failure
     */
    public int addSupplier(Supplier supplier, Connection conn) throws DatabaseException {
        String sql = "INSERT INTO suppliers (user_id, supplier_name, address, contact_no) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, supplier.getUserId());
            pstmt.setString(2, supplier.getSupplierName());
            pstmt.setString(3, supplier.getAddress());
            pstmt.setString(4, supplier.getContactNo());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating supplier failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    supplier.setSupplierId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Creating supplier failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting supplier into database: " + e.getMessage(), e);
        }
    }

    @Override
    public Supplier getById(int supplierId) throws DatabaseException {
        String sql = "SELECT s.supplier_id, s.user_id, s.supplier_name, s.address, s.contact_no, " +
                "u.username, u.password, u.phone_no, u.pin, u.role, u.status, u.created_at " +
                "FROM suppliers s JOIN users u ON s.user_id = u.user_id " +
                "WHERE s.supplier_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, supplierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSupplier(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving supplier by ID: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    public Supplier getSupplierByUserId(int userId) throws DatabaseException {
        String sql = "SELECT s.supplier_id, s.user_id, s.supplier_name, s.address, s.contact_no, " +
                "u.username, u.password, u.phone_no, u.pin, u.role, u.status, u.created_at " +
                "FROM suppliers s JOIN users u ON s.user_id = u.user_id " +
                "WHERE s.user_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSupplier(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving supplier by user ID: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    @Override
    public List<Supplier> getAll() throws DatabaseException {
        List<Supplier> supplierList = new ArrayList<>();
        String sql = "SELECT s.supplier_id, s.user_id, s.supplier_name, s.address, s.contact_no, " +
                "u.username, u.password, u.phone_no, u.pin, u.role, u.status, u.created_at " +
                "FROM suppliers s JOIN users u ON s.user_id = u.user_id " +
                "ORDER BY s.supplier_id ASC";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                supplierList.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all suppliers: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return supplierList;
    }

    public List<Supplier> getPendingSuppliers() throws DatabaseException {
        List<Supplier> pendingList = new ArrayList<>();
        String sql = "SELECT s.supplier_id, s.user_id, s.supplier_name, s.address, s.contact_no, " +
                "u.username, u.password, u.phone_no, u.pin, u.role, u.status, u.created_at " +
                "FROM suppliers s JOIN users u ON s.user_id = u.user_id " +
                "WHERE u.status = 'PENDING' ORDER BY s.supplier_id ASC";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                pendingList.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving pending suppliers: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return pendingList;
    }

    @Override
    public boolean update(Supplier supplier) throws DatabaseException {
        String sql = "UPDATE suppliers SET supplier_name = ?, address = ?, contact_no = ? WHERE supplier_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, supplier.getSupplierName());
            pstmt.setString(2, supplier.getAddress());
            pstmt.setString(3, supplier.getContactNo());
            pstmt.setInt(4, supplier.getSupplierId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating supplier: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    @Override
    public boolean delete(int supplierId) throws DatabaseException {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, supplierId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting supplier: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        return new Supplier(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("phone_no"),
                rs.getString("pin"),
                rs.getString("status"),
                rs.getTimestamp("created_at"),
                rs.getInt("supplier_id"),
                rs.getString("supplier_name"),
                rs.getString("address"),
                rs.getString("contact_no")
        );
    }
}
