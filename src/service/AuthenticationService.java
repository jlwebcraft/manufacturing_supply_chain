package service;

import dao.ManufacturerDAO;
import dao.SupplierDAO;
import dao.UserDAO;
import exception.AuthenticationException;
import exception.DatabaseException;
import exception.InvalidInputException;
import model.Manufacturer;
import model.Supplier;
import model.User;
import util.InputValidator;

/**
 * Authentication Service handling login verification and status/role authorization checks.
 */
public class AuthenticationService {

    private final UserDAO userDAO;
    private final ManufacturerDAO manufacturerDAO;
    private final SupplierDAO supplierDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
        this.manufacturerDAO = new ManufacturerDAO();
        this.supplierDAO = new SupplierDAO();
    }

    public AuthenticationService(UserDAO userDAO, ManufacturerDAO manufacturerDAO, SupplierDAO supplierDAO) {
        this.userDAO = userDAO;
        this.manufacturerDAO = manufacturerDAO;
        this.supplierDAO = supplierDAO;
    }

    /**
     * Authenticates an Admin user.
     * @param username Username
     * @param password Password
     * @return Logged-in User
     * @throws AuthenticationException on credentials failure or invalid status/role
     * @throws InvalidInputException on input validation failure
     * @throws DatabaseException on DB failure
     */
    public User loginAdmin(String username, String password) throws AuthenticationException, InvalidInputException, DatabaseException {
        InputValidator.validateUsername(username);
        InputValidator.validatePassword(password);

        User user = userDAO.getUserByUsername(username.trim());
        if (user == null || !user.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid username or password.");
        }

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new AuthenticationException("Access Denied: Requested role matches ADMIN, but account role is " + user.getRole() + ".");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AuthenticationException("Admin account is inactive. Current status: " + user.getStatus());
        }

        return user;
    }

    /**
     * Authenticates a Manufacturer user.
     * @param username Username
     * @param password Password
     * @return Complete Manufacturer object
     * @throws AuthenticationException on credentials/status failure
     * @throws InvalidInputException on input validation failure
     * @throws DatabaseException on DB failure
     */
    public Manufacturer loginManufacturer(String username, String password) throws AuthenticationException, InvalidInputException, DatabaseException {
        InputValidator.validateUsername(username);
        InputValidator.validatePassword(password);

        User user = userDAO.getUserByUsername(username.trim());
        if (user == null || !user.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid username or password.");
        }

        if (!"MANUFACTURER".equalsIgnoreCase(user.getRole())) {
            throw new AuthenticationException("Access Denied: Requested role matches MANUFACTURER, but account role is " + user.getRole() + ".");
        }

        checkManufacturerSupplierStatus(user.getStatus(), "Manufacturer");

        Manufacturer manufacturer = manufacturerDAO.getManufacturerByUserId(user.getUserId());
        if (manufacturer == null) {
            throw new AuthenticationException("Manufacturer record details not found for user ID: " + user.getUserId());
        }

        return manufacturer;
    }

    /**
     * Authenticates a Supplier user.
     * @param username Username
     * @param password Password
     * @return Complete Supplier object
     * @throws AuthenticationException on credentials/status failure
     * @throws InvalidInputException on input validation failure
     * @throws DatabaseException on DB failure
     */
    public Supplier loginSupplier(String username, String password) throws AuthenticationException, InvalidInputException, DatabaseException {
        InputValidator.validateUsername(username);
        InputValidator.validatePassword(password);

        User user = userDAO.getUserByUsername(username.trim());
        if (user == null || !user.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid username or password.");
        }

        if (!"SUPPLIER".equalsIgnoreCase(user.getRole())) {
            throw new AuthenticationException("Access Denied: Requested role matches SUPPLIER, but account role is " + user.getRole() + ".");
        }

        checkManufacturerSupplierStatus(user.getStatus(), "Supplier");

        Supplier supplier = supplierDAO.getSupplierByUserId(user.getUserId());
        if (supplier == null) {
            throw new AuthenticationException("Supplier record details not found for user ID: " + user.getUserId());
        }

        return supplier;
    }

    /**
     * Authenticates a Customer user (Integration with Member 4).
     * @param phoneNo Customer phone number
     * @param pin Customer numeric PIN
     * @return Logged-in Customer User object
     * @throws AuthenticationException on credentials/status failure
     * @throws InvalidInputException on input validation failure
     * @throws DatabaseException on DB failure
     */
    public User loginCustomer(String phoneNo, String pin) throws AuthenticationException, InvalidInputException, DatabaseException {
        InputValidator.validatePhone(phoneNo);
        InputValidator.validatePin(pin);

        User user = userDAO.getUserByPhone(phoneNo.trim());
        if (user == null || !pin.trim().equals(user.getPin())) {
            throw new AuthenticationException("Invalid phone number or PIN.");
        }

        if (!"CUSTOMER".equalsIgnoreCase(user.getRole())) {
            throw new AuthenticationException("Access Denied: Account associated with this phone number is not a Customer.");
        }

        if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AuthenticationException("Customer account is currently inactive.");
        }

        return user;
    }

    private void checkManufacturerSupplierStatus(String status, String roleName) throws AuthenticationException {
        if ("PENDING".equalsIgnoreCase(status)) {
            throw new AuthenticationException(roleName + " account registration is pending admin approval.");
        }
        if ("REJECTED".equalsIgnoreCase(status)) {
            throw new AuthenticationException(roleName + " account registration has been rejected by admin.");
        }
        if ("INACTIVE".equalsIgnoreCase(status)) {
            throw new AuthenticationException(roleName + " account is currently inactive.");
        }
        if (!"APPROVED".equalsIgnoreCase(status) && !"ACTIVE".equalsIgnoreCase(status)) {
            throw new AuthenticationException(roleName + " account status (" + status + ") does not permit login.");
        }
    }
}
