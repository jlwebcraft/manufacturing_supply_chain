package customer.model;

public class OrderItemRequest {
    private int productId;
    private int quantity;

    public OrderItemRequest(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
