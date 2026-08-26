package customer.service;

import customer.dao.CustomerShipmentDAO;
import customer.model.Customer;
import customer.model.CustomerShipmentView;
import exception.CustomerNotFoundException;

import java.sql.SQLException;
import java.util.List;

public class CustomerShipmentService {
    private final CustomerShipmentDAO customerShipmentDAO;

    public CustomerShipmentService() {
        this(new CustomerShipmentDAO());
    }

    public CustomerShipmentService(CustomerShipmentDAO customerShipmentDAO) {
        this.customerShipmentDAO = customerShipmentDAO;
    }

    public List<CustomerShipmentView> getShipments(Customer customer)
            throws SQLException, CustomerNotFoundException {
        validateCustomer(customer);
        return customerShipmentDAO.getShipmentsByCustomerId(customer.getCustomerId());
    }

    public CustomerShipmentView getShipmentByOrderId(Customer customer, int orderId)
            throws SQLException, CustomerNotFoundException {
        validateCustomer(customer);
        validateOrderId(orderId);
        return customerShipmentDAO.getShipmentByOrderIdForCustomer(orderId, customer.getCustomerId());
    }

    public CustomerShipmentView getShipmentByShipmentId(Customer customer, int shipmentId)
            throws SQLException, CustomerNotFoundException {
        validateCustomer(customer);
        validateShipmentId(shipmentId);
        return customerShipmentDAO.getShipmentByIdForCustomer(shipmentId, customer.getCustomerId());
    }

    private void validateCustomer(Customer customer) throws CustomerNotFoundException {
        if (customer == null || customer.getCustomerId() <= 0) {
            throw new CustomerNotFoundException("Valid logged-in customer is required.");
        }
    }

    private void validateOrderId(int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number.");
        }
    }

    private void validateShipmentId(int shipmentId) {
        if (shipmentId <= 0) {
            throw new IllegalArgumentException("Shipment ID must be a positive number.");
        }
    }
}
