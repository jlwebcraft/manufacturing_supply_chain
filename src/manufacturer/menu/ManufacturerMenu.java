package manufacturer.menu;

import manufacturer.bom.menu.ProductMaterialMenu;
import manufacturer.category.menu.CategoryMenu;
import manufacturer.inventory.menu.ManufacturerInventoryMenu;
import manufacturer.machine.menu.MachineMenu;
import manufacturer.product.menu.ProductMenu;
import manufacturer.productionorder.menu.ProductionOrderMenu;
import manufacturer.productionrequest.menu.ProductionRequestMenu;
import manufacturer.qualitycheck.menu.QualityCheckMenu;
import manufacturer.rawmaterial.menu.RawMaterialMenu;
import manufacturer.scheduling.menu.ProductionSchedulingMenu;
import manufacturer.worker.menu.WorkerMenu;
import java.util.Scanner;

/** Parent terminal menu that delegates business work to existing Manufacturer module menus. */
public class ManufacturerMenu {
 private final Scanner scanner=new Scanner(System.in);
 private final CategoryMenu categoryMenu=new CategoryMenu();
 private final ProductMenu productMenu=new ProductMenu();
 private final RawMaterialMenu rawMaterialMenu=new RawMaterialMenu();
 private final ProductMaterialMenu productMaterialMenu=new ProductMaterialMenu();
 private final WorkerMenu workerMenu=new WorkerMenu();
 private final MachineMenu machineMenu=new MachineMenu();
 private final ProductionRequestMenu requestMenu=new ProductionRequestMenu();
 private final ProductionOrderMenu orderMenu=new ProductionOrderMenu();
 private final ProductionSchedulingMenu schedulingMenu=new ProductionSchedulingMenu();
 private final QualityCheckMenu qualityMenu=new QualityCheckMenu();
 private final ManufacturerInventoryMenu inventoryMenu=new ManufacturerInventoryMenu();
 /** Returns to the caller (future login/main menu) when Logout is selected. */
 public void showMenu(int manufacturerId){
     boolean run=true;while(run){printMenu();try{switch(scanner.nextLine().trim()){
 case"1":categoryMenu.showMenu();break;
 case"2":productMenu.showMenu(manufacturerId);break;
 case"3":rawMaterialMenu.showMenu(manufacturerId);break;
 case"4":productMaterialMenu.showMenu(manufacturerId);break;
 case"5":workerMenu.showMenu(manufacturerId);break;
 case"6":machineMenu.showMenu(manufacturerId);break;
 case"7":requestMenu.showMenu(manufacturerId);break;
 case"8":orderMenu.showMenu(manufacturerId);break;
 case"9":schedulingMenu.showMenu(manufacturerId);break;
 case"10":qualityMenu.showMenu(manufacturerId);break;
 case"11":inventoryMenu.showInventoryMenu(manufacturerId);break;
 case"12":inventoryMenu.showTransactionMenu(manufacturerId);break;
 case"13":run=false;break;default:throw new IllegalArgumentException("Choose 1 to 13.");
     }}
     catch(IllegalArgumentException e){System.out.println("Input error: "+e.getMessage());}}}
 private void printMenu(){System.out.println("\n========================================\nMANUFACTURER MANAGEMENT\n========================================\n1. Category Management\n2. Product Management\n3. Raw Material Management\n4. Product Material Management\n5. Worker Management\n6. Machine Management\n7. Production Requests\n8. Production Orders\n9. Production Scheduling\n10. Quality Check\n11. View Inventory\n12. Inventory Transactions\n13. Logout");System.out.print("Enter Choice: ");}
}
