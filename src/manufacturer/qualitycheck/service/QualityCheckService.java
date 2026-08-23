package manufacturer.qualitycheck.service;

import database.DBConnection;
import manufacturer.productionorder.model.ProductionOrder;
import manufacturer.qualitycheck.dao.QualityCheckDAO;
import manufacturer.qualitycheck.model.QualityCheck;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Quality-check validation and transactional production-order status workflow. */
public class QualityCheckService {
    private final QualityCheckDAO qualityCheckDAO = new QualityCheckDAO();

    public void validateManufacturer(int manufacturerId) throws SQLException {
        if (manufacturerId <= 0) {
            throw new IllegalArgumentException("Manufacturer ID must be a positive number.");
        }
        if (!qualityCheckDAO.manufacturerExists(manufacturerId)) {
            throw new IllegalArgumentException("Manufacturer ID does not exist.");
        }
    }

    public List<ProductionOrder> viewCompletedOrders(int manufacturerId) throws SQLException {
        validateManufacturer(manufacturerId);
        return qualityCheckDAO.findCompletedOrders(manufacturerId);
    }

    public int performQualityCheck(int productionOrderId, int manufacturerId, String result, String remarks)
            throws SQLException {
        validateManufacturer(manufacturerId);
        if (productionOrderId <= 0) {
            throw new IllegalArgumentException("Production Order ID must be a positive number.");
        }
        validateResult(result);
        if (remarks != null && remarks.trim().length() > 255) {
            throw new IllegalArgumentException("Remarks cannot exceed 255 characters.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ProductionOrder order = qualityCheckDAO.findOrderById(connection, productionOrderId);
                validateOrder(order, manufacturerId);
                if (!"COMPLETED".equals(order.getStatus())) {
                    throw new IllegalArgumentException("Only COMPLETED production orders can be quality checked.");
                }
                if (qualityCheckDAO.qualityCheckExists(connection, productionOrderId)) {
                    throw new IllegalArgumentException("A quality check already exists for this production order.");
                }
                int qualityCheckId = qualityCheckDAO.insertQualityCheck(connection, productionOrderId, result, remarks);
                String orderStatus = "PASSED".equals(result) ? "READY_FOR_SHIPMENT" : "FAILED";
                if (!qualityCheckDAO.updateOrderStatus(connection, productionOrderId, manufacturerId, orderStatus)) {
                    throw new SQLException("Production order status could not be updated.");
                }
                connection.commit();
                return qualityCheckId;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } catch (IllegalArgumentException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public List<QualityCheck> viewQualityCheckHistory(int manufacturerId) throws SQLException {
        validateManufacturer(manufacturerId);
        return qualityCheckDAO.findHistoryByManufacturer(manufacturerId);
    }

    private void validateResult(String result) {
        if (!"PASSED".equals(result) && !"FAILED".equals(result)) {
            throw new IllegalArgumentException("Quality result must be PASSED or FAILED.");
        }
    }

    private void validateOrder(ProductionOrder order, int manufacturerId) {
        if (order == null) {
            throw new IllegalArgumentException("Production Order ID does not exist.");
        }
        if (order.getManufacturerId() != manufacturerId) {
            throw new IllegalArgumentException("This production order belongs to another manufacturer.");
        }
    }
}
