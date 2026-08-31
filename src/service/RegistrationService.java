package service;

import dao.ManufacturerDAO;
import dao.SupplierDAO;
import dao.UserDAO;
import exception.DatabaseException;
import exception.InvalidInputException;
import model.Manufacturer;
import model.Supplier;
import model.User;
import util.DBConnection;
import util.InputValidator;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Registration Service for registering new Manufacturers and Suppliers.
 * Enforces JDBC transaction management across 'users' and role-specific tables.
 */
public class RegistrationService {

    private final UserDAO userDAO;
    private final ManufacturerDAO manufacturerDAO;
    private final SupplierDAO supplierDAO;

    public RegistrationService() {
        this.userDAO = new UserDAO();
        this.manufacturerDAO = new ManufacturerDAO();
        this.supplierDAO = new SupplierDAO();
    }

    public RegistrationService(UserDAO userDAO, ManufacturerDAO manufacturerDAO, SupplierDAO supplierDAO) {
        this.userDAO = userDAO;
        this.manufacturerDAO = manufacturerDAO;
        this.supplierDAO = supplierDAO;
    }

    /**
     * Registers a new Manufacturer entity using a two-table JDBC transaction.
     * Table 1: users (role = MANUFACTURER, status = PENDING)
     * Table 2: manufacturers
     *
     * @param username Username
     * @param password Password
     * @param phoneNo Phone Number
     * @param pin PIN
     * @param manufacturerName Business name
     * @param address Business address
     * @param contactNo Contact number
     * @return Registered Manufacturer object
     * @throws InvalidInputException if input format or unique constraint fails
     * @throws DatabaseException on database error
     */
    public Manufacturer registerManufacturer(String username, String password, String phoneNo, String pin,
                                             String manufacturerName, String address, String contactNo)
            throws InvalidInputException, DatabaseException {

        // 1. Input Validation
        InputValidator.validateUsername(username);
        InputValidator.validatePassword(password);
        InputValidator.validatePhone(phoneNo);
        InputValidator.validatePin(pin);
        InputValidator.validateRequired(manufacturerName, "Manufacturer Name");
        InputValidator.validateRequired(address, "Address");
        InputValidator.validateRequired(contactNo, "Contact Number");

        // 2. Uniqueness check
        if (userDAO.getUserByUsername(username.trim()) != null) {
            throw new InvalidInputException("Username '" + username + "' is already registered.");
        }
        if (userDAO.getUserByPhone(phoneNo.trim()) != null) {
            throw new InvalidInputException("Phone number '" + phoneNo + "' is already registered.");
        }

        Manufacturer manufacturer = new Manufacturer(
                username.trim(),
                password,
                phoneNo.trim(),
                pin.trim(),
                manufacturerName.trim(),
                address.trim(),
                contactNo.trim()
        );

        // 3. JDBC Transaction Management
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);

            // Step A: Insert into users
            int userId = userDAO.addUser(manufacturer, conn);
            manufacturer.setUserId(userId);

            // Step B: Insert into manufacturers
            int manufacturerId = manufacturerDAO.addManufacturer(manufacturer, conn);
            manufacturer.setManufacturerId(manufacturerId);

            // Commit transaction
            conn.commit();
            return manufacturer;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Transaction rollback failed: " + rollbackEx.getMessage());
                }
            }
            if (e instanceof DatabaseException) {
                throw (DatabaseException) e;
            }
            throw new DatabaseException("Transaction failed during manufacturer registration: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Failed to reset auto-commit: " + e.getMessage());
                }
                DBConnection.closeConnection(conn);
            }
        }
    }

    /**
     * Registers a new Supplier entity using a two-table JDBC transaction.
     * Table 1: users (role = SUPPLIER, status = PENDING)
     * Table 2: suppliers
     *
     * @param username Username
     * @param password Password
     * @param phoneNo Phone Number
     * @param pin PIN
     * @param supplierName Business name
     * @param address Business address
     * @param contactNo Contact number
     * @return Registered Supplier object
     * @throws InvalidInputException if input format or unique constraint fails
     * @throws DatabaseException on database error
     */
    public Supplier registerSupplier(String username, String password, String phoneNo, String pin,
                                     String supplierName, String address, String contactNo)
            throws InvalidInputException, DatabaseException {

        // 1. Input Validation
        InputValidator.validateUsername(username);
        InputValidator.validatePassword(password);
        InputValidator.validatePhone(phoneNo);
        InputValidator.validatePin(pin);
        InputValidator.validateRequired(supplierName, "Supplier Name");
        InputValidator.validateRequired(address, "Address");
        InputValidator.validateRequired(contactNo, "Contact Number");

        // 2. Uniqueness check
        if (userDAO.getUserByUsername(username.trim()) != null) {
            throw new InvalidInputException("Username '" + username + "' is already registered.");
        }
        if (userDAO.getUserByPhone(phoneNo.trim()) != null) {
            throw new InvalidInputException("Phone number '" + phoneNo + "' is already registered.");
        }

        Supplier supplier = new Supplier(
                username.trim(),
                password,
                phoneNo.trim(),
                pin.trim(),
                supplierName.trim(),
                address.trim(),
                contactNo.trim()
        );

        // 3. JDBC Transaction Management
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);

            // Step A: Insert into users
            int userId = userDAO.addUser(supplier, conn);
            supplier.setUserId(userId);

            // Step B: Insert into suppliers
            int supplierId = supplierDAO.addSupplier(supplier, conn);
            supplier.setSupplierId(supplierId);

            // Commit transaction
            conn.commit();
            return supplier;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Transaction rollback failed: " + rollbackEx.getMessage());
                }
            }
            if (e instanceof DatabaseException) {
                throw (DatabaseException) e;
            }
            throw new DatabaseException("Transaction failed during supplier registration: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Failed to reset auto-commit: " + e.getMessage());
                }
                DBConnection.closeConnection(conn);
            }
        }
    }

    /**
     * Shared registration integration helper for Member 4 (Customer registration).
     * @param phoneNo Phone number to check
     * @return true if phone number is already registered
     * @throws DatabaseException on DB error
     */
    public boolean isPhoneRegistered(String phoneNo) throws DatabaseException {
        return userDAO.getUserByPhone(phoneNo != null ? phoneNo.trim() : "") != null;
    }

    protected Connection getConnection() throws DatabaseException {
        return DBConnection.getConnection();
    }
}
