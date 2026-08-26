package manufacturer.inventory.menu;
import manufacturer.inventory.model.*; import manufacturer.inventory.service.ManufacturerInventoryService; import java.sql.SQLException; import java.util.*;
/** Reusable read-only terminal menu for manufacturer inventory and transactions. */
public class ManufacturerInventoryMenu {
 private final ManufacturerInventoryService service=new ManufacturerInventoryService(); private final Scanner scanner=new Scanner(System.in);
 public void showInventoryMenu(int id){
     boolean run=true;while(run){System.out.println("\n--- Manufacturer Inventory ---\n1. View Material Inventory\n2. Search Material Inventory\n3. View Low-Stock Materials\n4. Back");
         System.out.print("Enter Choice: ");try{switch(scanner.nextLine().trim())
         {
             case"1":materials(service.view(id));break;
             case"2":System.out.print("Search: ");materials(service.search(id,scanner.nextLine()));break;
             case"3":materials(service.lowStock(id));break;
             case"4":run=false;break;default:throw new IllegalArgumentException("Choose 1 to 4.");
         }}
         catch(IllegalArgumentException|SQLException e){System.out.println("Error: "+e.getMessage());}}}
 public void showTransactionMenu(int id){
     boolean run=true;
     while(run){
         System.out.println("\n--- Inventory Transactions ---\n1. View All\n2. Search\n3. Filter ADD\n4. Filter DEDUCT\n5. Back");
         System.out.print("Enter Choice: ");
         try{String c=scanner.nextLine().trim();
             if("5".equals(c)){run=false;
                 continue;}
             String q=null,t=null;
             if("2".equals(c)){
                 System.out.print("Search: ");
                 q=scanner.nextLine();}
             else if("3".equals(c))t="ADD";
             else if("4".equals(c))t="DEDUCT";
             else if(!"1".equals(c))
                 throw new IllegalArgumentException("Choose 1 to 5.");
             transactions(service.transactions(id,q,t));}
         catch(IllegalArgumentException|SQLException e){
             System.out.println("Error: "+e.getMessage());}}}
 private void materials(List<ManufacturerInventory> a){
     if(a.isEmpty()){System.out.println("No inventory found.");
         return;}
     for(ManufacturerInventory x:a)System.out.println(x.materialId+" | "+x.materialName+" | Qty: "+x.quantity+" "+x.unit+" | Minimum: "+x.minimumStock);}
 private void transactions(List<InventoryTransaction>a){
     if(a.isEmpty()){System.out.println("No transactions found.");
         return;
     }
     for(InventoryTransaction x:a)System.out.println(x.transactionId+" | "+x.materialName+" | "+x.transactionType+" "+x.quantity+" | "+x.referenceType+" #"+x.referenceId+" | "+x.transactionDate);}
}
