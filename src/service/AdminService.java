package service;

import dao.ManufacturerDAO;
import dao.SupplierDAO;
import dao.UserDAO;
import exception.DatabaseException;
import exception.UserNotFoundException;
import model.Manufacturer;
import model.Supplier;
import model.User;
import util.InputValidator;

import java.util.List;

/**
 * Service class handling Admin management tasks including manufacturer & supplier approvals,
 * user search, user deactivation, and detailed view.
 */
public class AdminService {

    private final UserDAO userDAO;
    private final ManufacturerDAO manufacturerDAO;
    private final SupplierDAO supplierDAO;

    public AdminService() {
        this.userDAO = new UserDAO();
        this.manufacturerDAO = new ManufacturerDAO();
        this.supplierDAO = new SupplierDAO();
    }

    public AdminService(UserDAO userDAO, ManufacturerDAO manufacturerDAO, SupplierDAO supplierDAO) {
        this.userDAO = userDAO;
        this.manufacturerDAO = manufacturerDAO;
        this.supplierDAO = supplierDAO;
    }

    public List<Manufacturer> viewPendingManufacturers() throws DatabaseException {
        return manufacturerDAO.getPendingManufacturers();
    }

    public boolean approveManufacturer(int manufacturerId) throws UserNotFoundException, DatabaseException {
        Manufacturer manufacturer = manufacturerDAO.getById(manufacturerId);
        if (manufacturer == null) {
            throw new UserNotFoundException("Manufacturer not found with ID: " + manufacturerId);
        }
        if ("CUSTOMER".equalsIgnoreCase(manufacturer.getRole())) {
            throw new IllegalArgumentException("Admin cannot approve/reject customer accounts.");
        }
        return userDAO.updateUserStatus(manufacturer.getUserId(), "APPROVED");
    }

    public boolean rejectManufacturer(int manufacturerId) throws UserNotFoundException, DatabaseException {
        Manufacturer manufacturer = manufacturerDAO.getById(manufacturerId);
        if (manufacturer == null) {
            throw new UserNotFoundException("Manufacturer not found with ID: " + manufacturerId);
        }
        if ("CUSTOMER".equalsIgnoreCase(manufacturer.getRole())) {
            throw new IllegalArgumentException("Admin cannot approve/reject customer accounts.");
        }
        return userDAO.updateUserStatus(manufacturer.getUserId(), "REJECTED");
    }

    public List<Supplier> viewPendingSuppliers() throws DatabaseException {
        return supplierDAO.getPendingSuppliers();
    }

    public boolean approveSupplier(int supplierId) throws UserNotFoundException, DatabaseException {
        Supplier supplier = supplierDAO.getById(supplierId);
        if (supplier == null) {
            throw new UserNotFoundException("Supplier not found with ID: " + supplierId);
        }
        if ("CUSTOMER".equalsIgnoreCase(supplier.getRole())) {
            throw new IllegalArgumentException("Admin cannot approve/reject customer accounts.");
        }
        return userDAO.updateUserStatus(supplier.getUserId(), "APPROVED");
    }

    public boolean rejectSupplier(int supplierId) throws UserNotFoundException, DatabaseException {
        Supplier supplier = supplierDAO.getById(supplierId);
        if (supplier == null) {
            throw new UserNotFoundException("Supplier not found with ID: " + supplierId);
        }
        if ("CUSTOMER".equalsIgnoreCase(supplier.getRole())) {
            throw new IllegalArgumentException("Admin cannot approve/reject customer accounts.");
        }
        return userDAO.updateUserStatus(supplier.getUserId(), "REJECTED");
    }

    public List<User> viewAllUsers() throws DatabaseException {
        return userDAO.getAll();
    }

    public User searchUser(String keyword) throws UserNotFoundException, DatabaseException {
        if (InputValidator.isNullOrEmpty(keyword)) {
            throw new UserNotFoundException("Search keyword cannot be empty.");
        }

        String query = keyword.trim();
        User user = null;

        // 1. Try parsing as userId if numeric
        if (query.matches("^[0-9]+$")) {
            try {
                int userId = Integer.parseInt(query);
                user = userDAO.getById(userId);
            } catch (NumberFormatException ignored) {
            }
        }

        // 2. Try by username
        if (user == null) {
            user = userDAO.getUserByUsername(query);
        }

        // 3. Try by phone number
        if (user == null) {
            user = userDAO.getUserByPhone(query);
        }

        if (user == null) {
            throw new UserNotFoundException("No user found matching search query '" + keyword + "'.");
        }

        return user;
    }

    public boolean deactivateUser(int userId) throws UserNotFoundException, DatabaseException {
        User user = userDAO.getById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        return userDAO.updateUserStatus(userId, "INACTIVE");
    }

    public User viewUserDetails(int userId) throws UserNotFoundException, DatabaseException {
        User user = userDAO.getById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        // If Manufacturer or Supplier, fetch complete details
        if ("MANUFACTURER".equalsIgnoreCase(user.getRole())) {
            Manufacturer manufacturer = manufacturerDAO.getManufacturerByUserId(userId);
            if (manufacturer != null) {
                return manufacturer;
            }
        } else if ("SUPPLIER".equalsIgnoreCase(user.getRole())) {
            Supplier supplier = supplierDAO.getSupplierByUserId(userId);
            if (supplier != null) {
                return supplier;
            }
        }

        return user;
    }
}
