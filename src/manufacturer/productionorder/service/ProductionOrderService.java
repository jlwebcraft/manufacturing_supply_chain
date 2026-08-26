package manufacturer.productionorder.service;

import database.DBConnection;
import manufacturer.machine.model.Machine;
import manufacturer.productionorder.dao.ProductionOrderDAO;
import manufacturer.productionorder.model.ProductionOrder;
import manufacturer.productionrequest.dao.ProductionRequestDAO;
import manufacturer.productionrequest.model.ProductionRequest;
import manufacturer.worker.model.Worker;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Transactional business workflow for manufacturer production orders. */
public class ProductionOrderService {
    private final ProductionOrderDAO productionOrderDAO = new ProductionOrderDAO();
    private final ProductionRequestDAO productionRequestDAO = new ProductionRequestDAO();

    public void validateManufacturer(int manufacturerId) throws SQLException {
        validatePositiveId(manufacturerId, "Manufacturer ID");
        if (!productionOrderDAO.manufacturerExists(manufacturerId)) {
            throw new IllegalArgumentException("Manufacturer ID does not exist.");
        }
    }

    public int createProductionOrder(int requestId, int manufacturerId) throws SQLException {
        validatePositiveId(requestId, "Request ID");
        validateManufacturer(manufacturerId);
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ProductionRequest request = productionRequestDAO.findById(connection, requestId);
                validateRequest(request, manufacturerId);
                if (!"APPROVED".equals(request.getStatus())) {
                    throw new IllegalArgumentException("A production order can be created only for an APPROVED request.");
                }
                if (productionOrderDAO.orderExistsForRequest(connection, requestId)) {
                    throw new IllegalArgumentException("A production order already exists for this request.");
                }
                int orderId = productionOrderDAO.createOrder(connection, requestId, request.getProductId(), manufacturerId,
                        request.getQuantity(), request.getPriority());
                connection.commit();
                return orderId;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } catch (IllegalArgumentException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public List<ProductionOrder> viewProductionOrders(int manufacturerId) throws SQLException {
        validateManufacturer(manufacturerId);
        return productionOrderDAO.findByManufacturer(manufacturerId);
    }

    public ProductionOrder getProductionOrderDetails(int orderId, int manufacturerId) throws SQLException {
        validatePositiveId(orderId, "Production Order ID");
        validateManufacturer(manufacturerId);
        ProductionOrder order = productionOrderDAO.findById(orderId);
        validateOrder(order, manufacturerId);
        return order;
    }

    public List<Worker> getAssignedWorkers(int orderId, int manufacturerId) throws SQLException {
        getProductionOrderDetails(orderId, manufacturerId);
        return productionOrderDAO.findAssignedWorkers(orderId);
    }

    public List<Machine> getAssignedMachines(int orderId, int manufacturerId) throws SQLException {
        getProductionOrderDetails(orderId, manufacturerId);
        return productionOrderDAO.findAssignedMachines(orderId);
    }

    public boolean assignWorker(int orderId, int workerId, int manufacturerId) throws SQLException {
        validatePositiveId(workerId, "Worker ID");
        return useTransaction(connection -> {
            ProductionOrder order = productionOrderDAO.findById(connection, orderId);
            validateOrder(order, manufacturerId);
            validateAssignableOrder(order);
            Worker worker = productionOrderDAO.findWorkerById(connection, workerId);
            validateWorker(worker, manufacturerId);
            if (!"AVAILABLE".equals(worker.getStatus())) {
                throw new IllegalArgumentException("Worker must be AVAILABLE before assignment.");
            }
            if (productionOrderDAO.workerAssignmentExists(connection, orderId, workerId)) {
                throw new IllegalArgumentException("Worker is already assigned to this production order.");
            }
            if (!productionOrderDAO.addWorkerAssignment(connection, orderId, workerId)
                    || !productionOrderDAO.updateWorkerStatus(connection, workerId, manufacturerId, "ASSIGNED")
                    || !productionOrderDAO.updateOrderStatus(connection, orderId, manufacturerId, "ASSIGNED")) {
                throw new SQLException("Worker assignment could not be completed.");
            }
            return true;
        });
    }

    public boolean assignMachine(int orderId, int machineId, int manufacturerId) throws SQLException {
        validatePositiveId(machineId, "Machine ID");
        return useTransaction(connection -> {
            ProductionOrder order = productionOrderDAO.findById(connection, orderId);
            validateOrder(order, manufacturerId);
            validateAssignableOrder(order);
            Machine machine = productionOrderDAO.findMachineById(connection, machineId);
            validateMachine(machine, manufacturerId);
            if (!"AVAILABLE".equals(machine.getStatus())) {
                throw new IllegalArgumentException("Machine must be AVAILABLE before assignment.");
            }
            if (productionOrderDAO.machineAssignmentExists(connection, orderId, machineId)) {
                throw new IllegalArgumentException("Machine is already assigned to this production order.");
            }
            if (!productionOrderDAO.addMachineAssignment(connection, orderId, machineId)
                    || !productionOrderDAO.updateOrderStatus(connection, orderId, manufacturerId, "ASSIGNED")) {
                throw new SQLException("Machine assignment could not be completed.");
            }
            return true;
        });
    }

    public boolean startProduction(int orderId, int manufacturerId) throws SQLException {
        return useTransaction(connection -> {
            ProductionOrder order = productionOrderDAO.findById(connection, orderId);
            validateOrder(order, manufacturerId);
            if (!"CREATED".equals(order.getStatus()) && !"ASSIGNED".equals(order.getStatus())) {
                throw new IllegalArgumentException("Only CREATED or ASSIGNED orders can start production.");
            }
            if (productionOrderDAO.countWorkerAssignments(connection, orderId) == 0
                    || productionOrderDAO.countMachineAssignments(connection, orderId) == 0) {
                throw new IllegalArgumentException("Assign at least one worker and one machine before starting production.");
            }
            List<Machine> machines = productionOrderDAO.findAssignedMachines(connection, orderId);
            for (Machine machine : machines) {
                if (!"AVAILABLE".equals(machine.getStatus())) {
                    throw new IllegalArgumentException("Assigned machine " + machine.getMachineName()
                            + " is not AVAILABLE.");
                }
            }
            for (Machine machine : machines) {
                if (!productionOrderDAO.updateMachineStatus(connection, machine.getMachineId(), manufacturerId, "IN_USE")) {
                    throw new SQLException("Machine status could not be updated.");
                }
            }
            if (!productionOrderDAO.startOrder(connection, orderId, manufacturerId)) {
                throw new SQLException("Production could not be started.");
            }
            return true;
        });
    }

    public boolean completeProduction(int orderId, int manufacturerId) throws SQLException {
        return useTransaction(connection -> {
            ProductionOrder order = productionOrderDAO.findById(connection, orderId);
            validateOrder(order, manufacturerId);
            if (!"IN_PRODUCTION".equals(order.getStatus())) {
                throw new IllegalArgumentException("Only an IN_PRODUCTION order can be completed.");
            }
            if (!productionOrderDAO.completeOrder(connection, orderId, manufacturerId)) {
                throw new SQLException("Production could not be completed.");
            }
            return true;
        });
    }

    public boolean sendForQualityCheck(int orderId, int manufacturerId) throws SQLException {
        return useTransaction(connection -> {
            ProductionOrder order = productionOrderDAO.findById(connection, orderId);
            validateOrder(order, manufacturerId);
            if (!"COMPLETED".equals(order.getStatus())) {
                throw new IllegalArgumentException("Only a COMPLETED order can be sent for quality check.");
            }
            if (!productionOrderDAO.sendForQualityCheck(connection, orderId, manufacturerId)) {
                throw new SQLException("Order could not be sent for quality check.");
            }
            return true;
        });
    }

    private void validateRequest(ProductionRequest request, int manufacturerId) {
        if (request == null) {
            throw new IllegalArgumentException("Production request does not exist.");
        }
        if (request.getManufacturerId() != manufacturerId) {
            throw new IllegalArgumentException("This production request belongs to another manufacturer.");
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

    private void validateAssignableOrder(ProductionOrder order) {
        if (!"CREATED".equals(order.getStatus()) && !"ASSIGNED".equals(order.getStatus())) {
            throw new IllegalArgumentException("Workers and machines can be assigned only before production starts.");
        }
    }

    private void validateWorker(Worker worker, int manufacturerId) {
        if (worker == null) {
            throw new IllegalArgumentException("Worker ID does not exist.");
        }
        if (worker.getManufacturerId() != manufacturerId) {
            throw new IllegalArgumentException("This worker belongs to another manufacturer.");
        }
    }

    private void validateMachine(Machine machine, int manufacturerId) {
        if (machine == null) {
            throw new IllegalArgumentException("Machine ID does not exist.");
        }
        if (machine.getManufacturerId() != manufacturerId) {
            throw new IllegalArgumentException("This machine belongs to another manufacturer.");
        }
    }

    private void validatePositiveId(int id, String label) {
        if (id <= 0) {
            throw new IllegalArgumentException(label + " must be a positive number.");
        }
    }

    private boolean useTransaction(TransactionWork work) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean result = work.execute(connection);
                connection.commit();
                return result;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } catch (IllegalArgumentException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private interface TransactionWork {
        boolean execute(Connection connection) throws SQLException;
    }
}
