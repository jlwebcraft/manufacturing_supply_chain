package manufacturer.scheduling.model;

import manufacturer.product.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Simple tree node: a category is a parent node and its products are leaf data. */
public class ProductCategoryNode {
    private final int categoryId;
    private final String categoryName;
    private final List<Product> products = new ArrayList<>();

    public ProductCategoryNode(int categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }

    public void addProduct(Product product) {
        products.add(product);
    }

    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }
}
