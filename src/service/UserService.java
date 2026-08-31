package service;

import dao.UserDAO;
import exception.DatabaseException;
import exception.InvalidInputException;
import exception.UserNotFoundException;
import model.User;
import util.InputValidator;

import java.util.List;

/**
 * Service class for managing General User operations.
 */
public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User getUserById(int userId) throws UserNotFoundException, DatabaseException {
        User user = userDAO.getById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        return user;
    }

    public User getUserByUsername(String username) throws UserNotFoundException, DatabaseException {
        if (InputValidator.isNullOrEmpty(username)) {
            throw new UserNotFoundException("Username cannot be empty.");
        }
        User user = userDAO.getUserByUsername(username.trim());
        if (user == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }
        return user;
    }

    public User getUserByPhone(String phoneNo) throws UserNotFoundException, DatabaseException {
        if (InputValidator.isNullOrEmpty(phoneNo)) {
            throw new UserNotFoundException("Phone number cannot be empty.");
        }
        User user = userDAO.getUserByPhone(phoneNo.trim());
        if (user == null) {
            throw new UserNotFoundException("User not found with phone number: " + phoneNo);
        }
        return user;
    }

    public List<User> getAllUsers() throws DatabaseException {
        return userDAO.getAll();
    }

    public boolean updateUserStatus(int userId, String newStatus) throws UserNotFoundException, DatabaseException, InvalidInputException {
        InputValidator.validateRequired(newStatus, "Status");
        String formattedStatus = newStatus.trim().toUpperCase();

        if (!isValidStatus(formattedStatus)) {
            throw new InvalidInputException("Invalid status value '" + newStatus + "'. Allowed statuses: PENDING, APPROVED, REJECTED, ACTIVE, INACTIVE.");
        }

        User user = getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        return userDAO.updateUserStatus(userId, formattedStatus);
    }

    public boolean deactivateUser(int userId) throws UserNotFoundException, DatabaseException {
        User user = getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        return userDAO.updateUserStatus(userId, "INACTIVE");
    }

    private boolean isValidStatus(String status) {
        return "PENDING".equals(status) ||
               "APPROVED".equals(status) ||
               "REJECTED".equals(status) ||
               "ACTIVE".equals(status) ||
               "INACTIVE".equals(status);
    }
}
