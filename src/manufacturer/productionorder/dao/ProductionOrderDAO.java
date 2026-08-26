package manufacturer.productionorder.dao;

import database.DBConnection;
import manufacturer.machine.model.Machine;
import manufacturer.productionorder.model.ProductionOrder;
import manufacturer.worker.model.Worker;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** JDBC operations for production_orders and its worker/machine assignment tables. */
public class ProductionOrderDAO {
    private static final String ORDER_DETAILS =
            "SELECT po.production_order_id, po.request_id, po.manufacturer_id, s.supplier_name, p.product_name, "
                    + "c.category_name, po.quantity, po.priority, po.start_date, po.completion_date, po.status "
                    + "FROM production_orders po "
                    + "INNER JOIN production_requests pr ON po.request_id = pr.request_id "
                    + "INNER JOIN suppliers s ON pr.supplier_id = s.supplier_id "
                    + "INNER JOIN products p ON po.product_id = p.product_id "
                    + "INNER JOIN categories c ON p.category_id = c.category_id ";

    public List<ProductionOrder> findByManufacturer(int manufacturerId) throws SQLException {
        String sql = ORDER_DETAILS + "WHERE po.manufacturer_id = ? ORDER BY po.production_order_id DESC";
        List<ProductionOrder> orders = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, manufacturerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(mapOrder(resultSet));
                }
            }
        }
        return orders;
    }

    public ProductionOrder findById(int productionOrderId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return findById(connection, productionOrderId);
        }
    }

    public ProductionOrder findById(Connection connection, int productionOrderId) throws SQLException {
        String sql = ORDER_DETAILS + "WHERE po.production_order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productionOrderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapOrder(resultSet) : null;
            }
        }
    }

    public boolean orderExistsForRequest(Connection connection, int requestId) throws SQLException {
        String sql = "SELECT production_order_id FROM production_orders WHERE request_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int createOrder(Connection connection, int requestId, int productId, int manufacturerId, int quantity,
                           String priority) throws SQLException {
        String sql = "INSERT INTO production_orders (request_id, product_id, manufacturer_id, quantity, priority, status) "
                + "VALUES (?, ?, ?, ?, ?, 'CREATED')";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, requestId);
            statement.setInt(2, productId);
            statement.setInt(3, manufacturerId);
            statement.setInt(4, quantity);
            statement.setString(5, priority);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public Worker findWorkerById(Connection connection, int workerId) throws SQLException {
        String sql = "SELECT worker_id, manufacturer_id, worker_name, skill, contact_no, status "
                + "FROM workers WHERE worker_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, workerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new Worker(resultSet.getInt("worker_id"), resultSet.getInt("manufacturer_id"),
                        resultSet.getString("worker_name"), resultSet.getString("skill"),
                        resultSet.getString("contact_no"), resultSet.getString("status"));
            }
        }
    }

    public Machine findMachineById(Connection connection, int machineId) throws SQLException {
        String sql = "SELECT machine_id, manufacturer_id, machine_name, machine_type, status "
                + "FROM machines WHERE machine_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, machineId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new Machine(resultSet.getInt("machine_id"), resultSet.getInt("manufacturer_id"),
                        resultSet.getString("machine_name"), resultSet.getString("machine_type"),
                        resultSet.getString("status"));
            }
        }
    }

    public boolean workerAssignmentExists(Connection connection, int orderId, int workerId) throws SQLException {
        return assignmentExists(connection, "SELECT worker_id FROM production_workers WHERE production_order_id = ? AND worker_id = ?", orderId, workerId);
    }

    public boolean machineAssignmentExists(Connection connection, int orderId, int machineId) throws SQLException {
        return assignmentExists(connection, "SELECT machine_id FROM production_machines WHERE production_order_id = ? AND machine_id = ?", orderId, machineId);
    }

    public boolean addWorkerAssignment(Connection connection, int orderId, int workerId) throws SQLException {
        return addAssignment(connection, "INSERT INTO production_workers (production_order_id, worker_id) VALUES (?, ?)", orderId, workerId);
    }

    public boolean addMachineAssignment(Connection connection, int orderId, int machineId) throws SQLException {
        return addAssignment(connection, "INSERT INTO production_machines (production_order_id, machine_id) VALUES (?, ?)", orderId, machineId);
    }

    public boolean updateWorkerStatus(Connection connection, int workerId, int manufacturerId, String status) throws SQLException {
        return updateResourceStatus(connection, "UPDATE workers SET status = ? WHERE worker_id = ? AND manufacturer_id = ?", workerId, manufacturerId, status);
    }

    public boolean updateMachineStatus(Connection connection, int machineId, int manufacturerId, String status) throws SQLException {
        return updateResourceStatus(connection, "UPDATE machines SET status = ? WHERE machine_id = ? AND manufacturer_id = ?", machineId, manufacturerId, status);
    }

    public boolean updateOrderStatus(Connection connection, int orderId, int manufacturerId, String status) throws SQLException {
        String sql = "UPDATE production_orders SET status = ? WHERE production_order_id = ? AND manufacturer_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, orderId);
            statement.setInt(3, manufacturerId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean startOrder(Connection connection, int orderId, int manufacturerId) throws SQLException {
        String sql = "UPDATE production_orders SET status = 'IN_PRODUCTION', start_date = CURRENT_DATE "
                + "WHERE production_order_id = ? AND manufacturer_id = ? AND status IN ('CREATED', 'ASSIGNED')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, manufacturerId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean completeOrder(Connection connection, int orderId, int manufacturerId) throws SQLException {
        String sql = "UPDATE production_orders SET status = 'COMPLETED', completion_date = CURRENT_DATE "
                + "WHERE production_order_id = ? AND manufacturer_id = ? AND status = 'IN_PRODUCTION'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, manufacturerId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean sendForQualityCheck(Connection connection, int orderId, int manufacturerId) throws SQLException {
        String sql = "UPDATE production_orders SET status = 'QUALITY_CHECK' "
                + "WHERE production_order_id = ? AND manufacturer_id = ? AND status = 'COMPLETED'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, manufacturerId);
            return statement.executeUpdate() > 0;
        }
    }

    public List<Worker> findAssignedWorkers(int orderId) throws SQLException {
        String sql = "SELECT w.worker_id, w.manufacturer_id, w.worker_name, w.skill, w.contact_no, w.status "
                + "FROM production_workers pw INNER JOIN workers w ON pw.worker_id = w.worker_id "
                + "WHERE pw.production_order_id = ? ORDER BY w.worker_name";
        List<Worker> workers = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    workers.add(new Worker(resultSet.getInt("worker_id"), resultSet.getInt("manufacturer_id"),
                            resultSet.getString("worker_name"), resultSet.getString("skill"),
                            resultSet.getString("contact_no"), resultSet.getString("status")));
                }
            }
        }
        return workers;
    }

    public List<Machine> findAssignedMachines(int orderId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return findAssignedMachines(connection, orderId);
        }
    }

    public List<Machine> findAssignedMachines(Connection connection, int orderId) throws SQLException {
        String sql = "SELECT m.machine_id, m.manufacturer_id, m.machine_name, m.machine_type, m.status "
                + "FROM production_machines pm INNER JOIN machines m ON pm.machine_id = m.machine_id "
                + "WHERE pm.production_order_id = ? ORDER BY m.machine_name";
        List<Machine> machines = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    machines.add(new Machine(resultSet.getInt("machine_id"), resultSet.getInt("manufacturer_id"),
                            resultSet.getString("machine_name"), resultSet.getString("machine_type"),
                            resultSet.getString("status")));
                }
            }
        }
        return machines;
    }

    public int countWorkerAssignments(Connection connection, int orderId) throws SQLException {
        return countAssignments(connection, "SELECT COUNT(*) FROM production_workers WHERE production_order_id = ?", orderId);
    }

    public int countMachineAssignments(Connection connection, int orderId) throws SQLException {
        return countAssignments(connection, "SELECT COUNT(*) FROM production_machines WHERE production_order_id = ?", orderId);
    }

    public boolean manufacturerExists(int manufacturerId) throws SQLException {
        String sql = "SELECT manufacturer_id FROM manufacturers WHERE manufacturer_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, manufacturerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean assignmentExists(Connection connection, String sql, int orderId, int resourceId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, resourceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean addAssignment(Connection connection, String sql, int orderId, int resourceId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, resourceId);
            return statement.executeUpdate() > 0;
        }
    }

    private boolean updateResourceStatus(Connection connection, String sql, int resourceId, int manufacturerId, String status)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, resourceId);
            statement.setInt(3, manufacturerId);
            return statement.executeUpdate() > 0;
        }
    }

    private int countAssignments(Connection connection, String sql, int orderId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private ProductionOrder mapOrder(ResultSet resultSet) throws SQLException {
        return new ProductionOrder(resultSet.getInt("production_order_id"), resultSet.getInt("request_id"),
                resultSet.getInt("manufacturer_id"), resultSet.getString("supplier_name"),
                resultSet.getString("product_name"), resultSet.getString("category_name"),
                resultSet.getInt("quantity"), resultSet.getString("priority"), resultSet.getDate("start_date"),
                resultSet.getDate("completion_date"), resultSet.getString("status"));
    }
}
