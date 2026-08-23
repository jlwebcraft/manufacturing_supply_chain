package manufacturer.inventory.service;
import manufacturer.inventory.dao.ManufacturerInventoryDAO;
import manufacturer.inventory.model.*;
import java.sql.SQLException; import java.util.List;
/** Service layer for read-only inventory views. */
public class ManufacturerInventoryService {
 private final ManufacturerInventoryDAO dao=new ManufacturerInventoryDAO();
 public List<ManufacturerInventory> view(int id)throws SQLException{return dao.findMaterials(valid(id),null,false);}
 public List<ManufacturerInventory> search(int id,String text)throws SQLException{if(text==null||text.trim().isEmpty())throw new IllegalArgumentException("Search text cannot be empty.");return dao.findMaterials(valid(id),text.trim(),false);}
 public List<ManufacturerInventory> lowStock(int id)throws SQLException{return dao.findMaterials(valid(id),null,true);}
 public List<InventoryTransaction> transactions(int id,String text,String type)throws SQLException{return dao.findTransactions(valid(id),empty(text),type);}
 private int valid(int id){if(id<=0)throw new IllegalArgumentException("Manufacturer ID must be positive.");return id;} private String empty(String s){return s==null||s.trim().isEmpty()?null:s.trim();}
}
