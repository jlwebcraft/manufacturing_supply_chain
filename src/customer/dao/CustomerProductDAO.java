package customer.dao;

import customer.model.CustomerProductView;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerProductDAO {
    private static final String BASE_PRODUCT_SELECT =
            "SELECT p.product_id, p.product_name, c.category_name, p.description, "
                    + "p.unit_price, m.company_name AS manufacturer_name "
                    + "FROM products p "
                    + "INNER JOIN categories c ON p.category_id = c.category_id "
                    + "INNER JOIN manufacturers m ON p.manufacturer_id = m.manufacturer_id ";

    public List<CustomerProductView> getActiveProducts() throws SQLException {
        String sql = BASE_PRODUCT_SELECT
                + "WHERE p.status = 'ACTIVE' "
                + "ORDER BY p.product_name";

        List<CustomerProductView> products = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(mapProduct(resultSet));
            }
        }

        return products;
    }

    public List<CustomerProductView> searchActiveProductsByName(String searchText) throws SQLException {
        String sql = BASE_PRODUCT_SELECT
                + "WHERE p.status = 'ACTIVE' "
                + "AND LOWER(p.product_name) LIKE LOWER(?) "
                + "ORDER BY p.product_name";

        List<CustomerProductView> products = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + searchText + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
            }
        }

        return products;
    }

    public CustomerProductView getActiveProductDetailsById(int productId) throws SQLException {
        String sql = BASE_PRODUCT_SELECT
                + "WHERE p.status = 'ACTIVE' "
                + "AND p.product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapProduct(resultSet);
                }
            }
        }

        return null;
    }

    private CustomerProductView mapProduct(ResultSet resultSet) throws SQLException {
        return new CustomerProductView(
                resultSet.getInt("product_id"),
                resultSet.getString("product_name"),
                resultSet.getString("category_name"),
                resultSet.getString("description"),
                resultSet.getBigDecimal("unit_price"),
                resultSet.getString("manufacturer_name")
        );
    }
}
