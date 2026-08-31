package manufacturer.product.service;

import manufacturer.product.dao.ProductDAO;
import manufacturer.product.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** Product validation and manufacturer-side business rules. */
public class ProductService {
    private final ProductDAO productDAO = new ProductDAO();

    public int addProduct(int manufacturerId, int categoryId, String name, String description, BigDecimal price)
            throws SQLException {
        validateProduct(manufacturerId, categoryId, name, description, price);
        return productDAO.add(new Product(manufacturerId, categoryId, name.trim(), cleanDescription(description), price));
    }

    public List<Product> viewProducts() throws SQLException {
        return productDAO.findAll();
    }

    public List<Product> searchProducts(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search text cannot be empty.");
        }
        return productDAO.search(keyword.trim());
    }

    public Product viewProductDetails(int productId) throws SQLException {
        validatePositiveId(productId, "Product ID");
        return productDAO.findById(productId);
    }

    public boolean updateProduct(int productId, int manufacturerId, int categoryId, String name,
                                 String description, BigDecimal price) throws SQLException {
        validatePositiveId(productId, "Product ID");
        validateProduct(manufacturerId, categoryId, name, description, price);
        return productDAO.update(new Product(productId, manufacturerId, categoryId, name.trim(),
                cleanDescription(description), price, null, null));
    }

    public boolean deactivateProduct(int productId, int manufacturerId) throws SQLException {
        validatePositiveId(productId, "Product ID");
        validatePositiveId(manufacturerId, "Manufacturer ID");
        return productDAO.deactivate(productId, manufacturerId);
    }

    private void validateProduct(int manufacturerId, int categoryId, String name, String description, BigDecimal price)
            throws SQLException {
        validatePositiveId(manufacturerId, "Manufacturer ID");
        validatePositiveId(categoryId, "Category ID");
        if (!productDAO.manufacturerExists(manufacturerId)) {
            throw new IllegalArgumentException("Manufacturer ID does not exist.");
        }
        if (!productDAO.activeCategoryExists(categoryId)) {
            throw new IllegalArgumentException("Category ID does not exist or is inactive.");
        }
        if (name == null || name.trim().isEmpty() || name.trim().length() > 100) {
            throw new IllegalArgumentException("Product name is required and cannot exceed 100 characters.");
        }
        if (description != null && description.trim().length() > 255) {
            throw new IllegalArgumentException("Description cannot exceed 255 characters.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0 || price.scale() > 2) {
            throw new IllegalArgumentException("Price must be zero or positive with at most two decimal places.");
        }
    }

    private void validatePositiveId(int id, String fieldName) {
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive number.");
        }
    }

    private String cleanDescription(String description) {
        return description == null ? null : description.trim();
    }
}
