package dao;

import exception.DatabaseException;
import interfaces.CRUDOperations;
import model.User;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User entity.
 * Handles database operations for the 'users' table.
 */
public class UserDAO implements CRUDOperations<User> {

    @Override
    public boolean add(User user) throws DatabaseException {
        return addUser(user) > 0;
    }

    /**
     * Inserts a user record and returns the generated user_id.
     * Uses standalone connection.
     * @param user User object
     * @return Generated user_id
     * @throws DatabaseException on SQL failure
     */
    public int addUser(User user) throws DatabaseException {
        Connection conn = DBConnection.getConnection();
        try {
            return addUser(user, conn);
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    /**
     * Inserts a user record within an active transaction connection.
     * @param user User object
     * @param conn Database connection (managed externally for transactions)
     * @return Generated user_id
     * @throws DatabaseException on SQL failure
     */
    public int addUser(User user, Connection conn) throws DatabaseException {
        String sql = "INSERT INTO users (username, password, phone_no, pin, role, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getPhoneNo());
            pstmt.setString(4, user.getPin());
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getStatus());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    user.setUserId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error inserting user into database: " + e.getMessage(), e);
        }
    }

    @Override
    public User getById(int userId) throws DatabaseException {
        String sql = "SELECT user_id, username, password, phone_no, pin, role, status, created_at FROM users WHERE user_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving user by ID: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    public User getUserByUsername(String username) throws DatabaseException {
        String sql = "SELECT user_id, username, password, phone_no, pin, role, status, created_at FROM users WHERE username = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving user by username: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    public User getUserByPhone(String phoneNo) throws DatabaseException {
        String sql = "SELECT user_id, username, password, phone_no, pin, role, status, created_at FROM users WHERE phone_no = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving user by phone number: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    @Override
    public List<User> getAll() throws DatabaseException {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT user_id, username, password, phone_no, pin, role, status, created_at FROM users ORDER BY user_id ASC";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                userList.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all users: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return userList;
    }

    @Override
    public boolean update(User user) throws DatabaseException {
        String sql = "UPDATE users SET username = ?, password = ?, phone_no = ?, pin = ?, role = ?, status = ? WHERE user_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getPhoneNo());
            pstmt.setString(4, user.getPin());
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getStatus());
            pstmt.setInt(7, user.getUserId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    @Override
    public boolean delete(int userId) throws DatabaseException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting user: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    public boolean updateUserStatus(int userId, String status) throws DatabaseException {
        Connection conn = DBConnection.getConnection();
        try {
            return updateUserStatus(userId, status, conn);
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    public boolean updateUserStatus(int userId, String status, Connection conn) throws DatabaseException {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user status: " + e.getMessage(), e);
        }
    }

    public User authenticateByUsername(String username, String password) throws DatabaseException {
        String sql = "SELECT user_id, username, password, phone_no, pin, role, status, created_at FROM users WHERE username = ? AND password = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error authenticating user by username: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    public User authenticateByPhone(String phoneNo, String pin) throws DatabaseException {
        String sql = "SELECT user_id, username, password, phone_no, pin, role, status, created_at FROM users WHERE phone_no = ? AND pin = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phoneNo);
            pstmt.setString(2, pin);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error authenticating user by phone: " + e.getMessage(), e);
        } finally {
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("phone_no"),
                rs.getString("pin"),
                rs.getString("role"),
                rs.getString("status"),
                rs.getTimestamp("created_at")
        );
    }
}
