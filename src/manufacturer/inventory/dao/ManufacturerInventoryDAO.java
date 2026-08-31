package manufacturer.inventory.dao;

import database.DBConnection;
import manufacturer.inventory.model.InventoryTransaction;
import manufacturer.inventory.model.ManufacturerInventory;
import java.sql.*;
import java.util.*;

/** JDBC read operations for existing manufacturer inventory and transactions. */
public class ManufacturerInventoryDAO {
    public List<ManufacturerInventory> findMaterials(int manufacturerId, String keyword, boolean lowStock) throws SQLException {
        String sql = "SELECT i.inventory_id, rm.material_id, rm.material_name, rm.unit, i.quantity, rm.minimum_stock "
                + "FROM inventory i INNER JOIN raw_materials rm ON i.material_id=rm.material_id "
                + "WHERE i.owner_type='MANUFACTURER' AND i.manufacturer_id=? "
                + "AND (? IS NULL OR rm.material_name LIKE ? OR CAST(rm.material_id AS CHAR) LIKE ?) "
                + "AND (?=0 OR i.quantity <= rm.minimum_stock) ORDER BY rm.material_name";
        List<ManufacturerInventory> list = new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            String value=keyword==null?null:"%"+keyword+"%"; p.setInt(1,manufacturerId); p.setString(2,value); p.setString(3,value); p.setString(4,value); p.setInt(5,lowStock?1:0);
            try(ResultSet r=p.executeQuery()){while(r.next()) list.add(new ManufacturerInventory(r.getInt("inventory_id"),r.getInt("material_id"),r.getString("material_name"),r.getString("unit"),r.getInt("quantity"),r.getInt("minimum_stock")));}
        } return list;
    }
    public List<InventoryTransaction> findTransactions(int manufacturerId, String keyword, String type) throws SQLException {
        String sql="SELECT it.transaction_id,it.inventory_id,rm.material_id,rm.material_name,it.transaction_type,it.quantity,it.reference_type,it.reference_id,it.transaction_date "
                +"FROM inventory_transactions it INNER JOIN inventory i ON it.inventory_id=i.inventory_id INNER JOIN raw_materials rm ON i.material_id=rm.material_id "
                +"WHERE i.owner_type='MANUFACTURER' AND i.manufacturer_id=? AND (? IS NULL OR it.transaction_type=?) "
                +"AND (? IS NULL OR rm.material_name LIKE ? OR it.reference_type LIKE ? OR CAST(it.reference_id AS CHAR) LIKE ?) ORDER BY it.transaction_date DESC";
        List<InventoryTransaction> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            String value=keyword==null?null:"%"+keyword+"%";p.setInt(1,manufacturerId);p.setString(2,type);p.setString(3,type);p.setString(4,value);p.setString(5,value);p.setString(6,value);p.setString(7,value);
            try(ResultSet r=p.executeQuery()){while(r.next())list.add(new InventoryTransaction(r.getInt("transaction_id"),r.getInt("inventory_id"),r.getInt("material_id"),r.getString("material_name"),r.getString("transaction_type"),r.getInt("quantity"),r.getString("reference_type"),r.getInt("reference_id"),r.getTimestamp("transaction_date")));}
        }return list;
    }
}
