package manufacturer.rawmaterial.service;

import manufacturer.rawmaterial.dao.RawMaterialDAO;
import manufacturer.rawmaterial.model.RawMaterial;

import java.sql.SQLException;
import java.util.List;

/** Validation and business rules for manufacturer raw materials. */
public class RawMaterialService {
    private final RawMaterialDAO rawMaterialDAO = new RawMaterialDAO();

    public int addRawMaterial(int manufacturerId, String name, String unit, int minimumStock) throws SQLException {
        validateMaterial(manufacturerId, name, unit, minimumStock);
        return rawMaterialDAO.add(new RawMaterial(manufacturerId, name.trim(), unit.trim(), minimumStock));
    }

    public void validateManufacturer(int manufacturerId) throws SQLException {
        validatePositiveId(manufacturerId, "Manufacturer ID");
        if (!rawMaterialDAO.manufacturerExists(manufacturerId)) {
            throw new IllegalArgumentException("Manufacturer ID does not exist.");
        }
    }

    public List<RawMaterial> viewMaterialsByStatus(int manufacturerId, String status) throws SQLException {
        validateManufacturer(manufacturerId);
        return rawMaterialDAO.findByManufacturerAndStatus(manufacturerId, status);
    }

    public List<RawMaterial> searchRawMaterials(int manufacturerId, String keyword) throws SQLException {
        validateManufacturer(manufacturerId);
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search text cannot be empty.");
        }
        return rawMaterialDAO.search(manufacturerId, keyword.trim());
    }

    public RawMaterial getMaterialForManufacturer(int materialId, int manufacturerId) throws SQLException {
        validatePositiveId(materialId, "Material ID");
        validateManufacturer(manufacturerId);
        RawMaterial material = rawMaterialDAO.findById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Material ID does not exist.");
        }
        if (material.getManufacturerId() != manufacturerId) {
            throw new IllegalArgumentException("This material belongs to another manufacturer.");
        }
        return material;
    }

    public boolean updateRawMaterial(int materialId, int manufacturerId, String name, String unit, int minimumStock)
            throws SQLException {
        RawMaterial existingMaterial = getMaterialForManufacturer(materialId, manufacturerId);
        if ("INACTIVE".equals(existingMaterial.getStatus())) {
            throw new IllegalArgumentException("This material is INACTIVE. Activate it separately before updating it.");
        }
        validateMaterial(manufacturerId, name, unit, minimumStock);
        return rawMaterialDAO.update(new RawMaterial(materialId, manufacturerId, name.trim(), unit.trim(),
                minimumStock, null, 0));
    }

    public boolean deactivateRawMaterial(int materialId, int manufacturerId) throws SQLException {
        RawMaterial material = getMaterialForManufacturer(materialId, manufacturerId);
        if ("INACTIVE".equals(material.getStatus())) {
            throw new IllegalArgumentException("Material is already INACTIVE.");
        }
        return rawMaterialDAO.deactivate(materialId, manufacturerId);
    }

    public boolean activateRawMaterial(int materialId, int manufacturerId) throws SQLException {
        RawMaterial material = getMaterialForManufacturer(materialId, manufacturerId);
        if ("ACTIVE".equals(material.getStatus())) {
            throw new IllegalArgumentException("Material is already ACTIVE.");
        }
        return rawMaterialDAO.activate(materialId, manufacturerId);
    }

    private void validateMaterial(int manufacturerId, String name, String unit, int minimumStock) throws SQLException {
        validateManufacturer(manufacturerId);
        if (name == null || name.trim().isEmpty() || name.trim().length() > 100) {
            throw new IllegalArgumentException("Material name is required and cannot exceed 100 characters.");
        }
        if (unit == null || unit.trim().isEmpty() || unit.trim().length() > 20) {
            throw new IllegalArgumentException("Unit is required and cannot exceed 20 characters.");
        }
        if (minimumStock < 0) {
            throw new IllegalArgumentException("Minimum stock cannot be negative.");
        }
    }

    private void validatePositiveId(int id, String label) {
        if (id <= 0) {
            throw new IllegalArgumentException(label + " must be a positive number.");
        }
    }
}
