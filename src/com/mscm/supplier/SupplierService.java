package com.mscm.supplier;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

/** Database operations for one authenticated supplier. Never expose a method without supplier scoping. */
public class SupplierService {
    private final int supplierId;

    public SupplierService(int supplierId) {
        if (supplierId <= 0) throw new IllegalArgumentException("A valid supplier ID is required.");
        this.supplierId = supplierId;
    }

    public void viewProfile() throws SQLException {
        String sql = "SELECT s.supplier_id, s.supplier_name, s.address, s.contact_no, u.username, u.phone_no "
                + "FROM suppliers s JOIN users u ON u.user_id=s.user_id WHERE s.supplier_id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, supplierId); try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) print("Supplier ID: " + rs.getInt(1) + "\nName: " + rs.getString(2) + "\nAddress: " + nullable(rs.getString(3)) + "\nContact: " + nullable(rs.getString(4)) + "\nUsername: " + rs.getString(5) + "\nLogin phone: " + nullable(rs.getString(6)));
                else print("Supplier profile was not found.");
            }
        }
    }

    public void updateProfile(String name, String address, String phone) throws SQLException {
        String sql = "UPDATE suppliers SET supplier_name=?, address=?, contact_no=? WHERE supplier_id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, address); ps.setString(3, phone); ps.setInt(4, supplierId);
            requireOne(ps.executeUpdate(), "Profile not found."); print("Supplier profile updated.");
        }
    }

    public void viewActiveProducts() throws SQLException { products("", false); }
    public void searchProducts(String search, boolean category) throws SQLException { products(search, category); }

    private void products(String search, boolean category) throws SQLException {
        String field = category ? "c.category_name" : "p.product_name";
        String sql = "SELECT p.product_id,p.product_name,m.manufacturer_id,m.manufacturer_name,c.category_name,p.price,"
                + "COALESCE(i.quantity,0) stock FROM products p JOIN manufacturers m ON m.manufacturer_id=p.manufacturer_id "
                + "JOIN categories c ON c.category_id=p.category_id LEFT JOIN inventory i ON i.product_id=p.product_id "
                + "AND i.supplier_id=? AND i.owner_type='SUPPLIER' WHERE p.status='ACTIVE' AND " + field + " LIKE ? ORDER BY p.product_name";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, supplierId); ps.setString(2, "%" + search + "%"); try (ResultSet rs = ps.executeQuery()) {
                print("\nID | Product | Manufacturer ID / Name | Category | Price | Your Stock");
                boolean found = false; while (rs.next()) { found = true; print(rs.getInt(1)+" | "+rs.getString(2)+" | "+rs.getInt(3)+" / "+rs.getString(4)+" | "+rs.getString(5)+" | "+rs.getBigDecimal(6)+" | "+rs.getInt(7)); }
                if (!found) print("No active products found.");
            }
        }
    }

    public void viewProductDetails(int productId) throws SQLException {
        String sql = "SELECT p.product_id,p.product_name,p.description,p.price,m.manufacturer_name,c.category_name,COALESCE(i.quantity,0) "
                + "FROM products p JOIN manufacturers m ON m.manufacturer_id=p.manufacturer_id JOIN categories c ON c.category_id=p.category_id "
                + "LEFT JOIN inventory i ON i.product_id=p.product_id AND i.supplier_id=? AND i.owner_type='SUPPLIER' WHERE p.product_id=? AND p.status='ACTIVE'";
        try (Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,supplierId); ps.setInt(2,productId); try(ResultSet rs=ps.executeQuery()) { if(rs.next()) print("Product: "+rs.getString(2)+" (ID "+rs.getInt(1)+")\nManufacturer: "+rs.getString(5)+"\nCategory: "+rs.getString(6)+"\nDescription: "+nullable(rs.getString(3))+"\nPrice: "+rs.getBigDecimal(4)+"\nYour stock: "+rs.getInt(7)); else print("Active product not found."); }
        }
    }

    public void createProductionRequest(int productId, int manufacturerId, int quantity, String priority, LocalDate requiredDate) throws SQLException {
        String valid = "SELECT 1 FROM products WHERE product_id=? AND manufacturer_id=? AND status='ACTIVE'";
        String insert = "INSERT INTO production_requests(supplier_id,manufacturer_id,product_id,quantity,priority,required_date,status) VALUES(?,?,?,?,?,?,'PENDING')";
        try(Connection c=DBConnection.getConnection(); PreparedStatement check=c.prepareStatement(valid); PreparedStatement ps=c.prepareStatement(insert)) {
            check.setInt(1,productId);check.setInt(2,manufacturerId);try(ResultSet rs=check.executeQuery()){if(!rs.next()){print("That product does not belong to the selected active manufacturer.");return;}}
            ps.setInt(1,supplierId);ps.setInt(2,manufacturerId);ps.setInt(3,productId);ps.setInt(4,quantity);ps.setString(5,priority);ps.setDate(6,Date.valueOf(requiredDate)); ps.executeUpdate(); print("Production request created with PENDING status.");
        }
    }

    public void viewProductionRequests() throws SQLException {
        String sql="SELECT pr.request_id,p.product_name,m.manufacturer_name,pr.quantity,pr.priority,pr.required_date,pr.request_date,pr.status FROM production_requests pr JOIN products p ON p.product_id=pr.product_id JOIN manufacturers m ON m.manufacturer_id=pr.manufacturer_id WHERE pr.supplier_id=? ORDER BY pr.request_date DESC";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,supplierId); printRows(ps,"Request | Product | Manufacturer | Qty | Priority | Required date | Requested | Status");}
    }

    public void viewReceivedProducts() throws SQLException {
        String sql="SELECT sh.shipment_id,po.production_order_id,p.product_name,po.quantity,sh.shipment_date,sh.delivery_date,sh.status FROM shipments sh JOIN production_orders po ON po.production_order_id=sh.production_order_id JOIN production_requests pr ON pr.request_id=po.request_id JOIN products p ON p.product_id=po.product_id WHERE sh.shipment_type='MANUFACTURER_TO_SUPPLIER' AND pr.supplier_id=? ORDER BY sh.shipment_id DESC";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,supplierId);printRows(ps,"Shipment | Production order | Product | Qty | Shipped | Delivered | Status");}
    }

    public void viewInventory() throws SQLException {
        String sql="SELECT i.inventory_id,p.product_id,p.product_name,p.price,i.quantity FROM inventory i JOIN products p ON p.product_id=i.product_id WHERE i.owner_type='SUPPLIER' AND i.supplier_id=? ORDER BY p.product_name";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,supplierId);printRows(ps,"Inventory | Product ID | Product | Price | Quantity");}
    }

    public void viewInventoryTransactions() throws SQLException {
        String sql="SELECT it.transaction_id,p.product_name,it.transaction_type,it.quantity,it.reference_type,it.reference_id,it.transaction_date FROM inventory_transactions it JOIN inventory i ON i.inventory_id=it.inventory_id JOIN products p ON p.product_id=i.product_id WHERE i.owner_type='SUPPLIER' AND i.supplier_id=? ORDER BY it.transaction_date DESC";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,supplierId);printRows(ps,"Transaction | Product | Type | Qty | Reference type | Reference ID | Date");}
    }

    public void viewCustomerOrders() throws SQLException {
        String sql="SELECT co.order_id,c.customer_name,co.order_date,co.total_amount,co.status FROM customer_orders co JOIN customers c ON c.customer_id=co.customer_id WHERE co.supplier_id=? ORDER BY co.order_date DESC";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,supplierId);printRows(ps,"Order | Customer | Date | Total | Status");}
    }

    public void viewOrderItems(int orderId) throws SQLException {
        if(!ownsOrder(orderId)) {print("Order not found for this supplier.");return;}
        String sql="SELECT oi.order_item_id,p.product_name,oi.quantity,oi.unit_price,(oi.quantity*oi.unit_price) line_total,COALESCE(i.quantity,0) available_stock FROM order_items oi JOIN products p ON p.product_id=oi.product_id LEFT JOIN inventory i ON i.product_id=oi.product_id AND i.supplier_id=? AND i.owner_type='SUPPLIER' WHERE oi.order_id=?";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,supplierId);ps.setInt(2,orderId);printRows(ps,"Item | Product | Qty | Unit price | Line total | Available stock");}
    }

    /** Inserts a sale only once. The database trigger performs the deduction; rows are locked and checked first. */
    public void confirmOrderAndCreateSale(int orderId) throws SQLException {
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                OrderInfo order = lockEligibleOrder(c,orderId,"PLACED");
                if(order == null) {c.rollback(); return;}
                if(!hasSufficientLockedStock(c,orderId)) {c.rollback(); print("Insufficient inventory. Order remains PLACED."); return;}
                try(PreparedStatement sale=c.prepareStatement("INSERT INTO sales(order_id,supplier_id,customer_id,total_amount,status) VALUES(?,?,?,?, 'CONFIRMED')")) {
                    sale.setInt(1,orderId);sale.setInt(2,supplierId);sale.setInt(3,order.customerId);sale.setBigDecimal(4,order.total);sale.executeUpdate();
                }
                try(PreparedStatement update=c.prepareStatement("UPDATE customer_orders SET status='CONFIRMED' WHERE order_id=? AND supplier_id=?")){update.setInt(1,orderId);update.setInt(2,supplierId);requireOne(update.executeUpdate(),"Order update failed.");}
                c.commit();print("Order confirmed; sale created and inventory deducted by the database trigger.");
            } catch(SQLException ex) {c.rollback();throw ex;} finally {c.setAutoCommit(true);}
        }
    }

    public void updateOrderStatus(int orderId,String target) throws SQLException {
        String current=currentOrderStatus(orderId); if(current==null){print("Order not found for this supplier.");return;}
        // A confirmed order already created a sale and the supplied schema has no reversal trigger.
        boolean allowed=(target.equals("REJECTED") && current.equals("PLACED")) || (target.equals("PROCESSING") && current.equals("CONFIRMED")) || (target.equals("CANCELLED") && current.equals("PLACED"));
        if(!allowed){print("Cannot change order from "+current+" to "+target+".");return;}
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE customer_orders SET status=? WHERE order_id=? AND supplier_id=?")){ps.setString(1,target);ps.setInt(2,orderId);ps.setInt(3,supplierId);requireOne(ps.executeUpdate(),"Order update failed.");print("Order status updated to "+target+".");}
    }

    public void viewSales() throws SQLException {
        String sql="SELECT s.sale_id,s.order_id,c.customer_name,s.sale_date,s.total_amount,s.status FROM sales s JOIN customers c ON c.customer_id=s.customer_id WHERE s.supplier_id=? ORDER BY s.sale_date DESC";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,supplierId);printRows(ps,"Sale | Order | Customer | Date | Amount | Status");}
    }

    public void updateSaleStatus(int saleId,String target) throws SQLException {
        String current=null;try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT status FROM sales WHERE sale_id=? AND supplier_id=?")){ps.setInt(1,saleId);ps.setInt(2,supplierId);try(ResultSet rs=ps.executeQuery()){if(rs.next())current=rs.getString(1);}}
        if(current==null){print("Sale not found for this supplier.");return;} if(!(current.equals("CONFIRMED") && target.equals("COMPLETED"))){print("Only a CONFIRMED sale can be marked COMPLETED.");return;}
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE sales SET status=? WHERE sale_id=? AND supplier_id=?")){ps.setString(1,target);ps.setInt(2,saleId);ps.setInt(3,supplierId);ps.executeUpdate();print("Sale status updated to "+target+".");}
    }

    public void viewCustomerShipments() throws SQLException {
        String sql="SELECT sh.shipment_id,sh.order_id,c.customer_name,sh.shipment_date,sh.delivery_date,sh.status FROM shipments sh JOIN customers c ON c.customer_id=sh.customer_id WHERE sh.shipment_type='SUPPLIER_TO_CUSTOMER' AND sh.supplier_id=? ORDER BY sh.shipment_id DESC";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,supplierId);printRows(ps,"Shipment | Order | Customer | Shipment date | Delivery date | Status");}
    }

    public void createCustomerShipment(int orderId,LocalDate shipmentDate) throws SQLException {
        String sql="INSERT INTO shipments(shipment_type,order_id,supplier_id,customer_id,shipment_date,status) SELECT 'SUPPLIER_TO_CUSTOMER',co.order_id,co.supplier_id,co.customer_id,?,'CREATED' FROM customer_orders co WHERE co.order_id=? AND co.supplier_id=? AND co.status='PROCESSING' AND NOT EXISTS(SELECT 1 FROM shipments sh WHERE sh.order_id=co.order_id AND sh.shipment_type='SUPPLIER_TO_CUSTOMER' AND sh.status<>'CANCELLED')";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setDate(1,Date.valueOf(shipmentDate));ps.setInt(2,orderId);ps.setInt(3,supplierId);if(ps.executeUpdate()==0)print("Shipment requires a PROCESSING order and no active shipment.");else print("Customer shipment created.");}
    }

    public void updateShipmentStatus(int shipmentId,String target) throws SQLException {
        try(Connection c=DBConnection.getConnection()) {c.setAutoCommit(false);try {
            String current=null;int orderId=0;try(PreparedStatement find=c.prepareStatement("SELECT status,order_id FROM shipments WHERE shipment_id=? AND supplier_id=? AND shipment_type='SUPPLIER_TO_CUSTOMER' FOR UPDATE")){find.setInt(1,shipmentId);find.setInt(2,supplierId);try(ResultSet rs=find.executeQuery()){if(rs.next()){current=rs.getString(1);orderId=rs.getInt(2);}}}
            if(current==null){c.rollback();print("Shipment not found for this supplier.");return;} if(!validShipmentTransition(current,target)){c.rollback();print("Cannot change shipment from "+current+" to "+target+".");return;}
            String sql="UPDATE shipments SET status=?, delivery_date=CASE WHEN ?='DELIVERED' THEN CURDATE() ELSE delivery_date END WHERE shipment_id=? AND supplier_id=?";try(PreparedStatement ps=c.prepareStatement(sql)){ps.setString(1,target);ps.setString(2,target);ps.setInt(3,shipmentId);ps.setInt(4,supplierId);ps.executeUpdate();}
            String orderStatus=target.equals("DISPATCHED")||target.equals("IN_TRANSIT")?"SHIPPED":target.equals("DELIVERED")?"DELIVERED":null;if(orderStatus!=null)try(PreparedStatement ps=c.prepareStatement("UPDATE customer_orders SET status=? WHERE order_id=? AND supplier_id=?")){ps.setString(1,orderStatus);ps.setInt(2,orderId);ps.setInt(3,supplierId);ps.executeUpdate();}
            c.commit();print("Shipment status updated to "+target+".");
        }catch(SQLException ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}}
    }

    private boolean ownsOrder(int id)throws SQLException{return currentOrderStatus(id)!=null;}
    private String currentOrderStatus(int id)throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT status FROM customer_orders WHERE order_id=? AND supplier_id=?")){ps.setInt(1,id);ps.setInt(2,supplierId);try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getString(1):null;}}}
    private OrderInfo lockEligibleOrder(Connection c,int id,String required)throws SQLException{try(PreparedStatement ps=c.prepareStatement("SELECT customer_id,total_amount,status FROM customer_orders WHERE order_id=? AND supplier_id=? FOR UPDATE")){ps.setInt(1,id);ps.setInt(2,supplierId);try(ResultSet rs=ps.executeQuery()){if(!rs.next()){print("Order not found for this supplier.");return null;}if(!required.equals(rs.getString(3))){print("Only "+required+" orders can be confirmed.");return null;}return new OrderInfo(rs.getInt(1),rs.getBigDecimal(2));}}}
    private boolean hasSufficientLockedStock(Connection c,int orderId)throws SQLException{String sql="SELECT oi.product_id,oi.quantity,COALESCE(i.quantity,0) FROM order_items oi LEFT JOIN inventory i ON i.product_id=oi.product_id AND i.supplier_id=? AND i.owner_type='SUPPLIER' WHERE oi.order_id=? FOR UPDATE";try(PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,supplierId);ps.setInt(2,orderId);try(ResultSet rs=ps.executeQuery()){boolean item=false;while(rs.next()){item=true;if(rs.getInt(3)<rs.getInt(2))return false;}return item;}}}
    private boolean validShipmentTransition(String from,String to){return (from.equals("CREATED")&&(to.equals("DISPATCHED")||to.equals("CANCELLED")))||(from.equals("DISPATCHED")&&(to.equals("IN_TRANSIT")||to.equals("CANCELLED")))||(from.equals("IN_TRANSIT")&&to.equals("DELIVERED"));}
    private void printRows(PreparedStatement ps,String heading)throws SQLException{try(ResultSet rs=ps.executeQuery()){print("\n"+heading);boolean any=false;ResultSetMetaData md=rs.getMetaData();while(rs.next()){any=true;StringBuilder row=new StringBuilder();for(int i=1;i<=md.getColumnCount();i++){if(i>1)row.append(" | ");row.append(nullable(rs.getString(i)));}print(row.toString());}if(!any)print("No records found.");}}
    private static void requireOne(int count,String message)throws SQLException{if(count!=1)throw new SQLException(message);} private static String nullable(String value){return value==null?"-":value;} private static void print(String message){System.out.println(message);} private static class OrderInfo{final int customerId;final BigDecimal total;OrderInfo(int customerId,BigDecimal total){this.customerId=customerId;this.total=total;}}
}
