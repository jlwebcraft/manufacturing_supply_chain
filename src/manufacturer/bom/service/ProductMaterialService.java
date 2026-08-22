package manufacturer.bom.service;

import manufacturer.bom.dao.ProductMaterialDAO;
import manufacturer.bom.model.ProductMaterialAssignment;
import manufacturer.product.model.Product;
import manufacturer.rawmaterial.model.RawMaterial;

import java.sql.SQLException;
import java.util.List;

/** Validation and ownership rules for product-to-material assignments. */
public class ProductMaterialService {
    private final ProductMaterialDAO productMaterialDAO = new ProductMaterialDAO();

    public void validateManufacturer(int manufacturerId) throws SQLException {
        validatePositiveId(manufacturerId, "Manufacturer ID");
        if (!productMaterialDAO.manufacturerExists(manufacturerId)) {
            throw new IllegalArgumentException("Manufacturer ID does not exist.");
        }
    }

    public List<Product> viewActiveProducts(int manufacturerId) throws SQLException {
        validateManufacturer(manufacturerId);
        return productMaterialDAO.findActiveProductsByManufacturer(manufacturerId);
    }

    public Product selectProduct(int productId, int manufacturerId) throws SQLException {
        validateManufacturer(manufacturerId);
        validatePositiveId(productId, "Product ID");
        Product product = productMaterialDAO.findProductById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product ID does not exist.");
        }
        if (product.getManufacturerId() != manufacturerId) {
            throw new IllegalArgumentException("This product belongs to another manufacturer.");
        }
        if (!"ACTIVE".equals(product.getStatus())) {
            throw new IllegalArgumentException("This product is INACTIVE and cannot be used for assignments.");
        }
        return product;
    }

    public List<ProductMaterialAssignment> viewAssignments(int productId, int manufacturerId) throws SQLException {
        selectProduct(productId, manufacturerId);
        return productMaterialDAO.findAssignments(productId);
    }

    public List<RawMaterial> viewActiveMaterials(int manufacturerId) throws SQLException {
        validateManufacturer(manufacturerId);
        return productMaterialDAO.findActiveMaterialsByManufacturer(manufacturerId);
    }

    public boolean assignMaterial(int productId, int materialId, int quantityRequired, int manufacturerId)
            throws SQLException {
        selectProduct(productId, manufacturerId);
        validateQuantity(quantityRequired);
        validateMaterial(materialId, manufacturerId);
        if (productMaterialDAO.assignmentExists(productId, materialId)) {
            throw new IllegalArgumentException("This raw material is already assigned to the selected product.");
        }
        return productMaterialDAO.addAssignment(productId, materialId, quantityRequired);
    }

    public boolean updateRequiredQuantity(int productId, int materialId, int quantityRequired, int manufacturerId)
            throws SQLException {
        selectProduct(productId, manufacturerId);
        validatePositiveId(materialId, "Material ID");
        validateQuantity(quantityRequired);
        if (!productMaterialDAO.assignmentExists(productId, materialId)) {
            throw new IllegalArgumentException("This raw material is not assigned to the selected product.");
        }
        return productMaterialDAO.updateQuantity(productId, materialId, quantityRequired);
    }

    public boolean removeMaterialAssignment(int productId, int materialId, int manufacturerId) throws SQLException {
        selectProduct(productId, manufacturerId);
        validatePositiveId(materialId, "Material ID");
        if (!productMaterialDAO.assignmentExists(productId, materialId)) {
            throw new IllegalArgumentException("This raw material is not assigned to the selected product.");
        }
        return productMaterialDAO.removeAssignment(productId, materialId);
    }

    private void validateMaterial(int materialId, int manufacturerId) throws SQLException {
        validatePositiveId(materialId, "Material ID");
        RawMaterial material = productMaterialDAO.findMaterialById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("Material ID does not exist.");
        }
        if (material.getManufacturerId() != manufacturerId) {
            throw new IllegalArgumentException("This raw material belongs to another manufacturer.");
        }
        if (!"ACTIVE".equals(material.getStatus())) {
            throw new IllegalArgumentException("This raw material is INACTIVE and cannot be assigned.");
        }
    }

    private void validateQuantity(int quantityRequired) {
        if (quantityRequired <= 0) {
            throw new IllegalArgumentException("Required quantity must be greater than zero.");
        }
    }

    private void validatePositiveId(int id, String label) {
        if (id <= 0) {
            throw new IllegalArgumentException(label + " must be a positive number.");
        }
    }
}
