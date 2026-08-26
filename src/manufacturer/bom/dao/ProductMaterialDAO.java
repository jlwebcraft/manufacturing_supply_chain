package manufacturer.bom.dao;

import database.DBConnection;
import manufacturer.bom.model.ProductMaterialAssignment;
import manufacturer.product.model.Product;
import manufacturer.rawmaterial.model.RawMaterial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** JDBC access for the existing product_materials Bill of Materials table. */
public class ProductMaterialDAO {
    public List<Product> findActiveProductsByManufacturer(int manufacturerId) throws SQLException {
        String sql = "SELECT product_id, manufacturer_id, category_id, product_name, description, price, status "
                + "FROM products WHERE manufacturer_id = ? AND status = 'ACTIVE' ORDER BY product_name";
        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, manufacturerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(new Product(resultSet.getInt("product_id"), resultSet.getInt("manufacturer_id"),
                            resultSet.getInt("category_id"), resultSet.getString("product_name"),
                            resultSet.getString("description"), resultSet.getBigDecimal("price"),
                            resultSet.getString("status"), null));
                }
            }
        }
        return products;
    }

    public Product findProductById(int productId) throws SQLException {
        String sql = "SELECT product_id, manufacturer_id, category_id, product_name, description, price, status "
                + "FROM products WHERE product_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new Product(resultSet.getInt("product_id"), resultSet.getInt("manufacturer_id"),
                        resultSet.getInt("category_id"), resultSet.getString("product_name"),
                        resultSet.getString("description"), resultSet.getBigDecimal("price"),
                        resultSet.getString("status"), null);
            }
        }
    }

    public List<RawMaterial> findActiveMaterialsByManufacturer(int manufacturerId) throws SQLException {
        String sql = "SELECT material_id, manufacturer_id, material_name, unit, minimum_stock, status "
                + "FROM raw_materials WHERE manufacturer_id = ? AND status = 'ACTIVE' ORDER BY material_name";
        List<RawMaterial> materials = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, manufacturerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    materials.add(new RawMaterial(resultSet.getInt("material_id"),
                            resultSet.getInt("manufacturer_id"), resultSet.getString("material_name"),
                            resultSet.getString("unit"), resultSet.getInt("minimum_stock"),
                            resultSet.getString("status"), 0));
                }
            }
        }
        return materials;
    }

    public RawMaterial findMaterialById(int materialId) throws SQLException {
        String sql = "SELECT material_id, manufacturer_id, material_name, unit, minimum_stock, status "
                + "FROM raw_materials WHERE material_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, materialId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new RawMaterial(resultSet.getInt("material_id"), resultSet.getInt("manufacturer_id"),
                        resultSet.getString("material_name"), resultSet.getString("unit"),
                        resultSet.getInt("minimum_stock"), resultSet.getString("status"), 0);
            }
        }
    }

    public List<ProductMaterialAssignment> findAssignments(int productId) throws SQLException {
        String sql = "SELECT p.product_id, p.product_name, rm.material_id, rm.material_name, "
                + "pm.quantity_required, rm.unit FROM product_materials pm "
                + "INNER JOIN products p ON pm.product_id = p.product_id "
                + "INNER JOIN raw_materials rm ON pm.material_id = rm.material_id "
                + "WHERE pm.product_id = ? ORDER BY rm.material_name";
        List<ProductMaterialAssignment> assignments = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    assignments.add(mapAssignment(resultSet));
                }
            }
        }
        return assignments;
    }

    public boolean assignmentExists(int productId, int materialId) throws SQLException {
        String sql = "SELECT product_id FROM product_materials WHERE product_id = ? AND material_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setInt(2, materialId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean addAssignment(int productId, int materialId, int quantityRequired) throws SQLException {
        String sql = "INSERT INTO product_materials (product_id, material_id, quantity_required) VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setInt(2, materialId);
            statement.setInt(3, quantityRequired);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateQuantity(int productId, int materialId, int quantityRequired) throws SQLException {
        String sql = "UPDATE product_materials SET quantity_required = ? WHERE product_id = ? AND material_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantityRequired);
            statement.setInt(2, productId);
            statement.setInt(3, materialId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean removeAssignment(int productId, int materialId) throws SQLException {
        String sql = "DELETE FROM product_materials WHERE product_id = ? AND material_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setInt(2, materialId);
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

    private ProductMaterialAssignment mapAssignment(ResultSet resultSet) throws SQLException {
        return new ProductMaterialAssignment(resultSet.getInt("product_id"), resultSet.getString("product_name"),
                resultSet.getInt("material_id"), resultSet.getString("material_name"),
                resultSet.getInt("quantity_required"), resultSet.getString("unit"));
    }
}
