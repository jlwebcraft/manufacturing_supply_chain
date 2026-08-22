package manufacturer.product.dao;

import database.DBConnection;
import manufacturer.product.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** JDBC data access for the existing products table only. */
public class ProductDAO {
    private static final String PRODUCT_WITH_CATEGORY =
            "SELECT p.product_id, p.manufacturer_id, p.category_id, p.product_name, "
                    + "p.description, p.price, p.status, c.category_name "
                    + "FROM products p INNER JOIN categories c ON p.category_id = c.category_id ";

    public int add(Product product) throws SQLException {
        String sql = "INSERT INTO products (manufacturer_id, category_id, product_name, description, price, status) "
                + "VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setProductValues(statement, product);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public List<Product> findAll() throws SQLException {
        return getProducts(PRODUCT_WITH_CATEGORY + "ORDER BY p.product_id", null, false);
    }

    public List<Product> search(String keyword) throws SQLException {
        String sql = PRODUCT_WITH_CATEGORY
                + "WHERE p.product_name LIKE ? OR p.description LIKE ? OR c.category_name LIKE ? "
                + "OR CAST(p.product_id AS CHAR) LIKE ? ORDER BY p.product_id";
        return getProducts(sql, keyword, true);
    }

    public Product findById(int productId) throws SQLException {
        String sql = PRODUCT_WITH_CATEGORY + "WHERE p.product_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapProduct(resultSet) : null;
            }
        }
    }

    public boolean update(Product product) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, product_name = ?, description = ?, price = ? "
                + "WHERE product_id = ? AND manufacturer_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, product.getCategoryId());
            statement.setString(2, product.getProductName());
            setDescription(statement, 3, product.getDescription());
            statement.setBigDecimal(4, product.getPrice());
            statement.setInt(5, product.getProductId());
            statement.setInt(6, product.getManufacturerId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deactivate(int productId, int manufacturerId) throws SQLException {
        String sql = "UPDATE products SET status = 'INACTIVE' "
                + "WHERE product_id = ? AND manufacturer_id = ? AND status = 'ACTIVE'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setInt(2, manufacturerId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean manufacturerExists(int manufacturerId) throws SQLException {
        return recordExists("SELECT manufacturer_id FROM manufacturers WHERE manufacturer_id = ?", manufacturerId);
    }

    public boolean activeCategoryExists(int categoryId) throws SQLException {
        return recordExists("SELECT category_id FROM categories WHERE category_id = ? AND status = 'ACTIVE'", categoryId);
    }

    private boolean recordExists(String sql, int id) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private List<Product> getProducts(String sql, String keyword, boolean isSearch) throws SQLException {
        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (isSearch) {
                String value = "%" + keyword + "%";
                for (int index = 1; index <= 4; index++) {
                    statement.setString(index, value);
                }
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
            }
        }
        return products;
    }

    private Product mapProduct(ResultSet resultSet) throws SQLException {
        return new Product(resultSet.getInt("product_id"), resultSet.getInt("manufacturer_id"),
                resultSet.getInt("category_id"), resultSet.getString("product_name"),
                resultSet.getString("description"), resultSet.getBigDecimal("price"),
                resultSet.getString("status"), resultSet.getString("category_name"));
    }

    private void setProductValues(PreparedStatement statement, Product product) throws SQLException {
        statement.setInt(1, product.getManufacturerId());
        statement.setInt(2, product.getCategoryId());
        statement.setString(3, product.getProductName());
        setDescription(statement, 4, product.getDescription());
        statement.setBigDecimal(5, product.getPrice());
    }

    private void setDescription(PreparedStatement statement, int index, String description) throws SQLException {
        if (description == null || description.trim().isEmpty()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, description.trim());
        }
    }
}
