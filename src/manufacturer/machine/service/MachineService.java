package manufacturer.machine.service;

import manufacturer.machine.dao.MachineDAO;
import manufacturer.machine.model.Machine;

import java.sql.SQLException;
import java.util.List;

/** Machine validation, ownership checks, and status rules. */
public class MachineService {
    private final MachineDAO machineDAO = new MachineDAO();

    public void validateManufacturer(int manufacturerId) throws SQLException {
        validatePositiveId(manufacturerId, "Manufacturer ID");
        if (!machineDAO.manufacturerExists(manufacturerId)) {
            throw new IllegalArgumentException("Manufacturer ID does not exist.");
        }
    }

    public int addMachine(int manufacturerId, String name, String type) throws SQLException {
        validateManufacturer(manufacturerId);
        validateMachineDetails(name, type);
        return machineDAO.add(new Machine(manufacturerId, name.trim(), cleanOptionalType(type)));
    }

    public List<Machine> viewMachines(int manufacturerId) throws SQLException {
        validateManufacturer(manufacturerId);
        return machineDAO.findByManufacturer(manufacturerId);
    }

    public List<Machine> searchMachines(int manufacturerId, String keyword) throws SQLException {
        validateManufacturer(manufacturerId);
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search text cannot be empty.");
        }
        return machineDAO.search(manufacturerId, keyword.trim());
    }

    public Machine getMachineForManufacturer(int machineId, int manufacturerId) throws SQLException {
        validatePositiveId(machineId, "Machine ID");
        validateManufacturer(manufacturerId);
        Machine machine = machineDAO.findById(machineId);
        if (machine == null) {
            throw new IllegalArgumentException("Machine ID does not exist.");
        }
        if (machine.getManufacturerId() != manufacturerId) {
            throw new IllegalArgumentException("This machine belongs to another manufacturer.");
        }
        return machine;
    }

    public boolean updateMachine(int machineId, int manufacturerId, String name, String type) throws SQLException {
        getMachineForManufacturer(machineId, manufacturerId);
        validateMachineDetails(name, type);
        return machineDAO.update(new Machine(machineId, manufacturerId, name.trim(), cleanOptionalType(type), null));
    }

    public boolean changeMachineStatus(int machineId, int manufacturerId, String status) throws SQLException {
        Machine machine = getMachineForManufacturer(machineId, manufacturerId);
        validateStatus(status);
        if (machine.getStatus().equals(status)) {
            throw new IllegalArgumentException("Machine is already " + status + ".");
        }
        return machineDAO.updateStatus(machineId, manufacturerId, status);
    }

    private void validateMachineDetails(String name, String type) {
        if (name == null || name.trim().isEmpty() || name.trim().length() > 100) {
            throw new IllegalArgumentException("Machine name is required and cannot exceed 100 characters.");
        }
        if (type != null && type.trim().length() > 100) {
            throw new IllegalArgumentException("Machine type cannot exceed 100 characters.");
        }
    }

    private void validateStatus(String status) {
        if (!"AVAILABLE".equals(status) && !"IN_USE".equals(status)
                && !"MAINTENANCE".equals(status) && !"INACTIVE".equals(status)) {
            throw new IllegalArgumentException("Invalid machine status.");
        }
    }

    private void validatePositiveId(int id, String label) {
        if (id <= 0) {
            throw new IllegalArgumentException(label + " must be a positive number.");
        }
    }

    private String cleanOptionalType(String type) {
        return type == null ? null : type.trim();
    }
}
