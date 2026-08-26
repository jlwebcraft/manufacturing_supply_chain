package manufacturer.scheduling.service;

import manufacturer.product.dao.ProductDAO;
import manufacturer.product.model.Product;
import manufacturer.productionorder.dao.ProductionOrderDAO;
import manufacturer.productionorder.model.ProductionOrder;
import manufacturer.productionrequest.dao.ProductionRequestDAO;
import manufacturer.productionrequest.model.ProductionRequest;
import manufacturer.scheduling.model.ProductCategoryNode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

/**
 * In-memory production scheduling support. MySQL remains the source of truth;
 * call refreshFromDatabase whenever the manufacturer enters the scheduling view.
 */
public class ProductionSchedulingService {
    private final ProductionRequestDAO productionRequestDAO = new ProductionRequestDAO();
    private final ProductionOrderDAO productionOrderDAO = new ProductionOrderDAO();
    private final ProductDAO productDAO = new ProductDAO();

    private final Queue<ProductionRequest> normalRequestQueue = new LinkedList<>();
    private final PriorityQueue<ProductionRequest> priorityRequestQueue = new PriorityQueue<>(
            Comparator.comparingInt(this::priorityRank).reversed()
                    .thenComparing(ProductionRequest::getRequiredDate, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(ProductionRequest::getRequestDate));
    private final ArrayList<ProductionOrder> productionOrders = new ArrayList<>();
    private final HashMap<Integer, Product> productLookup = new HashMap<>();
    private final LinkedList<ProductionOrder> productionHistory = new LinkedList<>();
    private final HashMap<Integer, Stack<String>> productionStatusHistory = new HashMap<>();
    private final HashMap<Integer, ProductCategoryNode> productCategoryTree = new HashMap<>();

    /** Reloads all collection views from existing database records for one manufacturer. */
    public void refreshFromDatabase(int manufacturerId) throws SQLException {
        validateManufacturer(manufacturerId);
        clearCollections();

        List<ProductionRequest> pendingRequests = productionRequestDAO.findPendingByManufacturer(manufacturerId);
        for (ProductionRequest request : pendingRequests) {
            priorityRequestQueue.offer(request);
            if ("NORMAL".equals(request.getPriority())) {
                normalRequestQueue.offer(request);
            }
        }

        for (Product product : productDAO.findAll()) {
            if (product.getManufacturerId() == manufacturerId) {
                productLookup.put(product.getProductId(), product);
                addProductToCategoryTree(product);
            }
        }

        for (ProductionOrder order : productionOrderDAO.findByManufacturer(manufacturerId)) {
            productionOrders.add(order);
            if (isHistorical(order.getStatus())) {
                productionHistory.add(order);
            }
            Stack<String> statusStack = new Stack<>();
            statusStack.push(order.getStatus());
            productionStatusHistory.put(order.getProductionOrderId(), statusStack);
        }
    }

    /** FIFO retrieval for a pending request whose priority is NORMAL. */
    public ProductionRequest pollNextNormalRequest() {
        return normalRequestQueue.poll();
    }

    /** Highest-priority retrieval: URGENT, HIGH, NORMAL, then LOW. */
    public ProductionRequest pollNextPriorityRequest() {
        return priorityRequestQueue.poll();
    }

    public List<ProductionRequest> getNormalRequestQueue() {
        return new ArrayList<>(normalRequestQueue);
    }

    public List<ProductionRequest> getPriorityRequestQueue() {
        PriorityQueue<ProductionRequest> copy = new PriorityQueue<>(priorityRequestQueue);
        List<ProductionRequest> requests = new ArrayList<>();
        while (!copy.isEmpty()) {
            requests.add(copy.poll());
        }
        return requests;
    }

    /** ArrayList-backed production-order display view. */
    public List<ProductionOrder> getProductionOrders() {
        return new ArrayList<>(productionOrders);
    }

    /** Fast lookup from product_id to an in-memory Product loaded from MySQL. */
    public Product findProductById(int productId) {
        return productLookup.get(productId);
    }

    /** LinkedList-backed view of completed/quality-check-and-later production orders. */
    public List<ProductionOrder> getProductionHistory() {
        return new LinkedList<>(productionHistory);
    }

    /**
     * Adds a status to the runtime Stack after a successful database status update.
     * This does not update production_orders; ProductionOrderService owns persistent changes.
     */
    public void recordProductionStatus(int productionOrderId, String status) {
        if (productionOrderId <= 0 || status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Production order ID and status are required.");
        }
        Stack<String> statusStack = productionStatusHistory.get(productionOrderId);
        if (statusStack == null) {
            statusStack = new Stack<>();
            productionStatusHistory.put(productionOrderId, statusStack);
        }
        statusStack.push(status.trim());
    }

    /** Returns the current runtime status trail from oldest to newest. */
    public List<String> getProductionStatusHistory(int productionOrderId) {
        Stack<String> statusStack = productionStatusHistory.get(productionOrderId);
        return statusStack == null ? new ArrayList<String>() : new ArrayList<>(statusStack);
    }

    /** Returns the category-rooted tree; each root contains products in that category. */
    public List<ProductCategoryNode> getProductCategoryTree() {
        return new ArrayList<>(productCategoryTree.values());
    }

    private void validateManufacturer(int manufacturerId) throws SQLException {
        if (manufacturerId <= 0) {
            throw new IllegalArgumentException("Manufacturer ID must be a positive number.");
        }
        if (!productionOrderDAO.manufacturerExists(manufacturerId)) {
            throw new IllegalArgumentException("Manufacturer ID does not exist.");
        }
    }

    private void clearCollections() {
        normalRequestQueue.clear();
        priorityRequestQueue.clear();
        productionOrders.clear();
        productLookup.clear();
        productionHistory.clear();
        productionStatusHistory.clear();
        productCategoryTree.clear();
    }

    private void addProductToCategoryTree(Product product) {
        ProductCategoryNode categoryNode = productCategoryTree.get(product.getCategoryId());
        if (categoryNode == null) {
            categoryNode = new ProductCategoryNode(product.getCategoryId(), product.getCategoryName());
            productCategoryTree.put(product.getCategoryId(), categoryNode);
        }
        categoryNode.addProduct(product);
    }

    private boolean isHistorical(String status) {
        return "COMPLETED".equals(status) || "QUALITY_CHECK".equals(status) || "PASSED".equals(status)
                || "FAILED".equals(status) || "READY_FOR_SHIPMENT".equals(status) || "SHIPPED".equals(status)
                || "DELIVERED".equals(status) || "CANCELLED".equals(status);
    }

    private int priorityRank(ProductionRequest request) {
        if ("URGENT".equals(request.getPriority())) {
            return 4;
        }
        if ("HIGH".equals(request.getPriority())) {
            return 3;
        }
        if ("NORMAL".equals(request.getPriority())) {
            return 2;
        }
        return 1;
    }
}
