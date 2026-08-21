package manufacturer.category.dao;

import database.DBConnection;
import manufacturer.category.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Performs JDBC CRUD operations only for the existing categories table. */
public class CategoryDAO {
    public int add(Category category) throws SQLException {
        String sql = "INSERT INTO categories (category_name, description, status) VALUES (?, ?, 'ACTIVE')";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, category.getCategoryName());
            setNullableDescription(statement, 2, category.getDescription());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Category> findAll() throws SQLException {
        String sql = "SELECT category_id, category_name, description, status FROM categories ORDER BY category_id";
        return getCategories(sql, null);
    }

    public List<Category> search(String keyword) throws SQLException {
        String sql = "SELECT category_id, category_name, description, status FROM categories "
                + "WHERE category_name LIKE ? OR description LIKE ? OR CAST(category_id AS CHAR) LIKE ? "
                + "ORDER BY category_id";
        return getCategories(sql, keyword);
    }

    public boolean update(Category category) throws SQLException {
        String sql = "UPDATE categories SET category_name = ?, description = ? WHERE category_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.getCategoryName());
            setNullableDescription(statement, 2, category.getDescription());
            statement.setInt(3, category.getCategoryId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deactivate(int categoryId) throws SQLException {
        String sql = "UPDATE categories SET status = 'INACTIVE' WHERE category_id = ? AND status = 'ACTIVE'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean categoryNameExists(String categoryName, int excludedCategoryId) throws SQLException {
        String sql = "SELECT category_id FROM categories WHERE category_name = ? AND category_id <> ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, categoryName);
            statement.setInt(2, excludedCategoryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private List<Category> getCategories(String sql, String keyword) throws SQLException {
        List<Category> categories = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (keyword != null) {
                String searchValue = "%" + keyword + "%";
                statement.setString(1, searchValue);
                statement.setString(2, searchValue);
                statement.setString(3, searchValue);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    categories.add(new Category(
                            resultSet.getInt("category_id"),
                            resultSet.getString("category_name"),
                            resultSet.getString("description"),
                            resultSet.getString("status")));
                }
            }
        }
        return categories;
    }

    private void setNullableDescription(PreparedStatement statement, int index, String description) throws SQLException {
        if (description == null || description.trim().isEmpty()) {
            statement.setNull(index, java.sql.Types.VARCHAR);
        } else {
            statement.setString(index, description.trim());
        }
    }
}
