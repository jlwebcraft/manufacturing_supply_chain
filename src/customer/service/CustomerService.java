package customer.service;

import customer.dao.CustomerDAO;
import customer.dao.CustomerProductDAO;
import customer.model.Customer;
import customer.model.CustomerProductView;
import customer.model.CustomerProfileView;
import exception.CustomerNotFoundException;
import exception.InvalidLoginException;

import java.sql.SQLException;
import java.util.List;

public class CustomerService {
    private final CustomerDAO customerDAO;
    private final CustomerProductDAO customerProductDAO;

    public CustomerService() {
        this(new CustomerDAO(), new CustomerProductDAO());
    }

    public CustomerService(CustomerDAO customerDAO, CustomerProductDAO customerProductDAO) {
        this.customerDAO = customerDAO;
        this.customerProductDAO = customerProductDAO;
    }

    public Customer login(String phoneNo, String pin) throws SQLException, InvalidLoginException {
        if (isBlank(phoneNo) || isBlank(pin)) {
            throw new InvalidLoginException("Phone number and PIN are required.");
        }

        return customerDAO.getCustomerByPhoneAndPin(phoneNo.trim(), pin.trim());
    }

    public List<CustomerProductView> viewProducts() throws SQLException {
        return customerProductDAO.getActiveProducts();
    }

    public List<CustomerProductView> searchProducts(String searchText) throws SQLException {
        if (isBlank(searchText)) {
            throw new IllegalArgumentException("Search text cannot be empty.");
        }

        return customerProductDAO.searchActiveProductsByName(searchText.trim());
    }

    public CustomerProductView getProductDetails(int productId) throws SQLException {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number.");
        }

        return customerProductDAO.getActiveProductDetailsById(productId);
    }

    public CustomerProfileView getCustomerProfile(Customer customer)
            throws SQLException, CustomerNotFoundException {
        if (customer == null || customer.getCustomerId() <= 0) {
            throw new CustomerNotFoundException("Valid customer is required to view profile.");
        }

        return customerDAO.getCustomerProfileById(customer.getCustomerId());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
