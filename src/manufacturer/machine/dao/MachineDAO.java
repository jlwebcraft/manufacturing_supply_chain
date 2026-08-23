package manufacturer.machine.dao;

import database.DBConnection;
import manufacturer.machine.model.Machine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** JDBC CRUD operations for the existing machines table. */
public class MachineDAO {
    public int add(Machine machine) throws SQLException {
        String sql = "INSERT INTO machines (manufacturer_id, machine_name, machine_type, status) "
                + "VALUES (?, ?, ?, 'AVAILABLE')";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, machine.getManufacturerId());
            statement.setString(2, machine.getMachineName());
            setNullableMachineType(statement, 3, machine.getMachineType());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public List<Machine> findByManufacturer(int manufacturerId) throws SQLException {
        String sql = "SELECT machine_id, manufacturer_id, machine_name, machine_type, status "
                + "FROM machines WHERE manufacturer_id = ? ORDER BY machine_id";
        return getMachines(sql, manufacturerId, null);
    }

    public List<Machine> search(int manufacturerId, String keyword) throws SQLException {
        String sql = "SELECT machine_id, manufacturer_id, machine_name, machine_type, status FROM machines "
                + "WHERE manufacturer_id = ? AND (machine_name LIKE ? OR machine_type LIKE ? "
                + "OR CAST(machine_id AS CHAR) LIKE ?) ORDER BY machine_id";
        return getMachines(sql, manufacturerId, keyword);
    }

    public Machine findById(int machineId) throws SQLException {
        String sql = "SELECT machine_id, manufacturer_id, machine_name, machine_type, status "
                + "FROM machines WHERE machine_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, machineId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapMachine(resultSet) : null;
            }
        }
    }

    public boolean update(Machine machine) throws SQLException {
        String sql = "UPDATE machines SET machine_name = ?, machine_type = ? "
                + "WHERE machine_id = ? AND manufacturer_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, machine.getMachineName());
            setNullableMachineType(statement, 2, machine.getMachineType());
            statement.setInt(3, machine.getMachineId());
            statement.setInt(4, machine.getManufacturerId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int machineId, int manufacturerId, String status) throws SQLException {
        String sql = "UPDATE machines SET status = ? WHERE machine_id = ? AND manufacturer_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, machineId);
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

    private List<Machine> getMachines(String sql, int manufacturerId, String keyword) throws SQLException {
        List<Machine> machines = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, manufacturerId);
            if (keyword != null) {
                String value = "%" + keyword + "%";
                statement.setString(2, value);
                statement.setString(3, value);
                statement.setString(4, value);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    machines.add(mapMachine(resultSet));
                }
            }
        }
        return machines;
    }

    private Machine mapMachine(ResultSet resultSet) throws SQLException {
        return new Machine(resultSet.getInt("machine_id"), resultSet.getInt("manufacturer_id"),
                resultSet.getString("machine_name"), resultSet.getString("machine_type"),
                resultSet.getString("status"));
    }

    private void setNullableMachineType(PreparedStatement statement, int index, String machineType) throws SQLException {
        if (machineType == null || machineType.trim().isEmpty()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, machineType.trim());
        }
    }
}
