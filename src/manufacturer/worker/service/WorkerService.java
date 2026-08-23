package manufacturer.worker.service;

import manufacturer.worker.dao.WorkerDAO;
import manufacturer.worker.model.Worker;

import java.sql.SQLException;
import java.util.List;

/** Worker validation, ownership checks, and status rules. */
public class WorkerService {
    private final WorkerDAO workerDAO = new WorkerDAO();

    public void validateManufacturer(int manufacturerId) throws SQLException {
        validatePositiveId(manufacturerId, "Manufacturer ID");
        if (!workerDAO.manufacturerExists(manufacturerId)) {
            throw new IllegalArgumentException("Manufacturer ID does not exist.");
        }
    }

    public int addWorker(int manufacturerId, String name, String skill, String contactNo) throws SQLException {
        validateManufacturer(manufacturerId);
        validateWorkerDetails(name, skill, contactNo);
        return workerDAO.add(new Worker(manufacturerId, name.trim(), cleanOptionalValue(skill),
                cleanOptionalValue(contactNo)));
    }

    public List<Worker> viewWorkers(int manufacturerId) throws SQLException {
        validateManufacturer(manufacturerId);
        return workerDAO.findByManufacturer(manufacturerId);
    }

    public List<Worker> searchWorkers(int manufacturerId, String keyword) throws SQLException {
        validateManufacturer(manufacturerId);
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search text cannot be empty.");
        }
        return workerDAO.search(manufacturerId, keyword.trim());
    }

    public Worker getWorkerForManufacturer(int workerId, int manufacturerId) throws SQLException {
        validatePositiveId(workerId, "Worker ID");
        validateManufacturer(manufacturerId);
        Worker worker = workerDAO.findById(workerId);
        if (worker == null) {
            throw new IllegalArgumentException("Worker ID does not exist.");
        }
        if (worker.getManufacturerId() != manufacturerId) {
            throw new IllegalArgumentException("This worker belongs to another manufacturer.");
        }
        return worker;
    }

    public boolean updateWorker(int workerId, int manufacturerId, String name, String skill, String contactNo)
            throws SQLException {
        getWorkerForManufacturer(workerId, manufacturerId);
        validateWorkerDetails(name, skill, contactNo);
        return workerDAO.update(new Worker(workerId, manufacturerId, name.trim(), cleanOptionalValue(skill),
                cleanOptionalValue(contactNo), null));
    }

    public boolean changeWorkerStatus(int workerId, int manufacturerId, String status) throws SQLException {
        Worker worker = getWorkerForManufacturer(workerId, manufacturerId);
        validateStatus(status);
        if (worker.getStatus().equals(status)) {
            throw new IllegalArgumentException("Worker is already " + status + ".");
        }
        return workerDAO.updateStatus(workerId, manufacturerId, status);
    }

    private void validateWorkerDetails(String name, String skill, String contactNo) {
        if (name == null || name.trim().isEmpty() || name.trim().length() > 100) {
            throw new IllegalArgumentException("Worker name is required and cannot exceed 100 characters.");
        }
        if (skill != null && skill.trim().length() > 100) {
            throw new IllegalArgumentException("Skill cannot exceed 100 characters.");
        }
        if (contactNo != null && contactNo.trim().length() > 15) {
            throw new IllegalArgumentException("Contact number cannot exceed 15 characters.");
        }
    }

    private void validateStatus(String status) {
        if (!"AVAILABLE".equals(status) && !"ASSIGNED".equals(status)
                && !"ON_LEAVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new IllegalArgumentException("Invalid worker status.");
        }
    }

    private void validatePositiveId(int id, String label) {
        if (id <= 0) {
            throw new IllegalArgumentException(label + " must be a positive number.");
        }
    }

    private String cleanOptionalValue(String value) {
        return value == null ? null : value.trim();
    }
}
