package customer.dao;

import customer.model.Customer;
import customer.model.CustomerProfileView;
import exception.CustomerNotFoundException;
import exception.InvalidLoginException;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerDAO {
    public int createCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (user_id, customer_name, address) VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, customer.getUserId());
            statement.setString(2, customer.getCustomerName());
            statement.setString(3, customer.getAddress());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int customerId = generatedKeys.getInt(1);
                    customer.setCustomerId(customerId);
                    return customerId;
                }
            }
        }

        throw new SQLException("Creating customer failed. No generated customer ID returned.");
    }

    public Customer getCustomerById(int customerId) throws SQLException, CustomerNotFoundException {
        String sql = "SELECT customer_id, user_id, customer_name, address FROM customers WHERE customer_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapCustomer(resultSet);
                }
            }
        }

        throw new CustomerNotFoundException("Customer not found with ID: " + customerId);
    }

    public Customer getCustomerByUserId(int userId) throws SQLException, CustomerNotFoundException {
        String sql = "SELECT customer_id, user_id, customer_name, address FROM customers WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapCustomer(resultSet);
                }
            }
        }

        throw new CustomerNotFoundException("Customer not found for user ID: " + userId);
    }

    public Customer getCustomerByPhoneAndPin(String phoneNo, String pin) throws SQLException, InvalidLoginException {
        String sql = "SELECT c.customer_id, c.user_id, c.customer_name, c.address "
                + "FROM users u "
                + "INNER JOIN customers c ON u.user_id = c.user_id "
                + "WHERE u.phone_no = ? "
                + "AND u.pin = ? "
                + "AND u.role = 'CUSTOMER' "
                + "AND u.status = 'ACTIVE'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, phoneNo);
            statement.setString(2, pin);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapCustomer(resultSet);
                }
            }
        }

        throw new InvalidLoginException("Invalid customer phone number or PIN.");
    }

    public boolean updateCustomer(Customer customer) throws SQLException, CustomerNotFoundException {
        String sql = "UPDATE customers SET customer_name = ?, address = ? WHERE customer_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getCustomerName());
            statement.setString(2, customer.getAddress());
            statement.setInt(3, customer.getCustomerId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                return true;
            }
        }

        throw new CustomerNotFoundException("Customer not found with ID: " + customer.getCustomerId());
    }

    public boolean deleteCustomer(int customerId) throws SQLException, CustomerNotFoundException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                return true;
            }
        }

        throw new CustomerNotFoundException("Customer not found with ID: " + customerId);
    }

    public CustomerProfileView getCustomerProfileById(int customerId) throws SQLException, CustomerNotFoundException {
        String sql = "SELECT c.customer_id, c.customer_name, c.address, u.phone_no "
                + "FROM customers c "
                + "INNER JOIN users u ON c.user_id = u.user_id "
                + "WHERE c.customer_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new CustomerProfileView(
                            resultSet.getInt("customer_id"),
                            resultSet.getString("customer_name"),
                            resultSet.getString("address"),
                            resultSet.getString("phone_no")
                    );
                }
            }
        }

        throw new CustomerNotFoundException("Customer profile not found with ID: " + customerId);
    }

    private Customer mapCustomer(ResultSet resultSet) throws SQLException {
        return new Customer(
                resultSet.getInt("customer_id"),
                resultSet.getInt("user_id"),
                resultSet.getString("customer_name"),
                resultSet.getString("address")
        );
    }
}
