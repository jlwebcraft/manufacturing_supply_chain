package manufacturer.category.service;

import manufacturer.category.dao.CategoryDAO;
import manufacturer.category.model.Category;

import java.sql.SQLException;
import java.util.List;

/** Holds category validation and business rules. */
public class CategoryService {
    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    public int addCategory(String categoryName, String description) throws SQLException, IllegalArgumentException {
        validateCategory(categoryName, description);
        String cleanName = categoryName.trim();
        if (categoryDAO.categoryNameExists(cleanName, 0)) {
            throw new IllegalArgumentException("A category with this name already exists.");
        }
        return categoryDAO.add(new Category(cleanName, cleanDescription(description)));
    }

    public List<Category> viewAllCategories() throws SQLException {
        return categoryDAO.findAll();
    }

    public List<Category> searchCategories(String keyword) throws SQLException, IllegalArgumentException {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search text cannot be empty.");
        }
        return categoryDAO.search(keyword.trim());
    }

    public boolean updateCategory(int categoryId, String categoryName, String description)
            throws SQLException, IllegalArgumentException {
        validateCategoryId(categoryId);
        validateCategory(categoryName, description);
        String cleanName = categoryName.trim();
        if (categoryDAO.categoryNameExists(cleanName, categoryId)) {
            throw new IllegalArgumentException("A category with this name already exists.");
        }
        return categoryDAO.update(new Category(categoryId, cleanName, cleanDescription(description), null));
    }

    public boolean deactivateCategory(int categoryId) throws SQLException, IllegalArgumentException {
        validateCategoryId(categoryId);
        return categoryDAO.deactivate(categoryId);
    }

    private void validateCategory(int categoryId) {
        validateCategoryId(categoryId);
    }

    private void validateCategoryId(int categoryId) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Category ID must be a positive number.");
        }
    }

    private void validateCategory(String categoryName, String description) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        if (categoryName.trim().length() > 100) {
            throw new IllegalArgumentException("Category name cannot exceed 100 characters.");
        }
        if (description != null && description.trim().length() > 255) {
            throw new IllegalArgumentException("Description cannot exceed 255 characters.");
        }
    }

    private String cleanDescription(String description) {
        return description == null ? null : description.trim();
    }
}
