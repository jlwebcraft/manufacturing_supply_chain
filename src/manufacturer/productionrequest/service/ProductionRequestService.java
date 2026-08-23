package manufacturer.productionrequest.service;

import database.DBConnection;
import manufacturer.productionrequest.dao.ProductionRequestDAO;
import manufacturer.productionrequest.model.ProductionRequest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Manufacturer validation and approve/reject transaction handling. */
public class ProductionRequestService {
    private final ProductionRequestDAO productionRequestDAO = new ProductionRequestDAO();

    public void validateManufacturer(int manufacturerId) throws SQLException {
        validatePositiveId(manufacturerId, "Manufacturer ID");
        if (!productionRequestDAO.manufacturerExists(manufacturerId)) {
            throw new IllegalArgumentException("Manufacturer ID does not exist.");
        }
    }

    public List<ProductionRequest> viewPendingRequests(int manufacturerId) throws SQLException {
        validateManufacturer(manufacturerId);
        return productionRequestDAO.findPendingByManufacturer(manufacturerId);
    }

    public List<ProductionRequest> viewRequestHistory(int manufacturerId) throws SQLException {
        validateManufacturer(manufacturerId);
        return productionRequestDAO.findHistoryByManufacturer(manufacturerId);
    }

    public ProductionRequest getRequestDetails(int requestId, int manufacturerId) throws SQLException {
        validatePositiveId(requestId, "Request ID");
        validateManufacturer(manufacturerId);
        ProductionRequest request = productionRequestDAO.findById(requestId);
        validateRequestOwnership(request, manufacturerId);
        return request;
    }

    public boolean approveRequest(int requestId, int manufacturerId) throws SQLException {
        return changePendingRequestStatus(requestId, manufacturerId, "APPROVED");
    }

    public boolean rejectRequest(int requestId, int manufacturerId) throws SQLException {
        return changePendingRequestStatus(requestId, manufacturerId, "REJECTED");
    }

    private boolean changePendingRequestStatus(int requestId, int manufacturerId, String newStatus) throws SQLException {
        validatePositiveId(requestId, "Request ID");
        validateManufacturer(manufacturerId);
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ProductionRequest request = productionRequestDAO.findById(connection, requestId);
                validateRequestOwnership(request, manufacturerId);
                if (!"PENDING".equals(request.getStatus())) {
                    throw new IllegalArgumentException("Only PENDING requests can be " + newStatus.toLowerCase() + ".");
                }
                boolean changed = productionRequestDAO.updatePendingStatus(connection, requestId, manufacturerId, newStatus);
                if (!changed) {
                    throw new SQLException("Request status changed by another operation. Please refresh and try again.");
                }
                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } catch (IllegalArgumentException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private void validateRequestOwnership(ProductionRequest request, int manufacturerId) {
        if (request == null) {
            throw new IllegalArgumentException("Request ID does not exist.");
        }
        if (request.getManufacturerId() != manufacturerId) {
            throw new IllegalArgumentException("This request belongs to another manufacturer.");
        }
    }

    private void validatePositiveId(int id, String label) {
        if (id <= 0) {
            throw new IllegalArgumentException(label + " must be a positive number.");
        }
    }
}
