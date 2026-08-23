package manufacturer.worker.dao;

import database.DBConnection;
import manufacturer.worker.model.Worker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** JDBC CRUD operations for the existing workers table. */
public class WorkerDAO {
    public int add(Worker worker) throws SQLException {
        String sql = "INSERT INTO workers (manufacturer_id, worker_name, skill, contact_no, status) "
                + "VALUES (?, ?, ?, ?, 'AVAILABLE')";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, worker.getManufacturerId());
            statement.setString(2, worker.getWorkerName());
            setNullableString(statement, 3, worker.getSkill());
            setNullableString(statement, 4, worker.getContactNo());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public List<Worker> findByManufacturer(int manufacturerId) throws SQLException {
        String sql = "SELECT worker_id, manufacturer_id, worker_name, skill, contact_no, status "
                + "FROM workers WHERE manufacturer_id = ? ORDER BY worker_id";
        return getWorkers(sql, manufacturerId, null);
    }

    public List<Worker> search(int manufacturerId, String keyword) throws SQLException {
        String sql = "SELECT worker_id, manufacturer_id, worker_name, skill, contact_no, status "
                + "FROM workers WHERE manufacturer_id = ? AND (worker_name LIKE ? OR skill LIKE ? "
                + "OR contact_no LIKE ? OR CAST(worker_id AS CHAR) LIKE ?) ORDER BY worker_id";
        return getWorkers(sql, manufacturerId, keyword);
    }

    public Worker findById(int workerId) throws SQLException {
        String sql = "SELECT worker_id, manufacturer_id, worker_name, skill, contact_no, status "
                + "FROM workers WHERE worker_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, workerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapWorker(resultSet) : null;
            }
        }
    }

    public boolean update(Worker worker) throws SQLException {
        String sql = "UPDATE workers SET worker_name = ?, skill = ?, contact_no = ? "
                + "WHERE worker_id = ? AND manufacturer_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, worker.getWorkerName());
            setNullableString(statement, 2, worker.getSkill());
            setNullableString(statement, 3, worker.getContactNo());
            statement.setInt(4, worker.getWorkerId());
            statement.setInt(5, worker.getManufacturerId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int workerId, int manufacturerId, String status) throws SQLException {
        String sql = "UPDATE workers SET status = ? WHERE worker_id = ? AND manufacturer_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, workerId);
            statement.setInt(3, manufacturerId);
            return statement.executeUpdate() > 0;
        }
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

    private List<Worker> getWorkers(String sql, int manufacturerId, String keyword) throws SQLException {
        List<Worker> workers = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, manufacturerId);
            if (keyword != null) {
                String value = "%" + keyword + "%";
                statement.setString(2, value);
                statement.setString(3, value);
                statement.setString(4, value);
                statement.setString(5, value);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    workers.add(mapWorker(resultSet));
                }
            }
        }
        return workers;
    }

    private Worker mapWorker(ResultSet resultSet) throws SQLException {
        return new Worker(resultSet.getInt("worker_id"), resultSet.getInt("manufacturer_id"),
                resultSet.getString("worker_name"), resultSet.getString("skill"),
                resultSet.getString("contact_no"), resultSet.getString("status"));
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value.trim());
        }
    }
}
