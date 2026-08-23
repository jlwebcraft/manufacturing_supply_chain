package manufacturer.machine.menu;

import manufacturer.machine.model.Machine;
import manufacturer.machine.service.MachineService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/** Reusable terminal menu for machines belonging to a logged-in manufacturer. */
public class MachineMenu {
    private final MachineService machineService = new MachineService();
    private final Scanner scanner = new Scanner(System.in);

    public void showMenu(int manufacturerId) {
        try {
            machineService.validateManufacturer(manufacturerId);
        } catch (IllegalArgumentException exception) {
            System.out.println("Input error: " + exception.getMessage());
            return;
        } catch (SQLException exception) {
            System.out.println("Database error: " + exception.getMessage());
            return;
        }

        boolean running = true;
        while (running) {
            printMenu();
            try {
                switch (scanner.nextLine().trim()) {
                    case "1": addMachine(manufacturerId); break;
                    case "2": printMachines(machineService.viewMachines(manufacturerId)); break;
                    case "3": searchMachine(manufacturerId); break;
                    case "4": updateMachine(manufacturerId); break;
                    case "5": changeMachineStatus(manufacturerId); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice. Please choose 1 to 6.");
                }
            } catch (IllegalArgumentException exception) {
                System.out.println("Input error: " + exception.getMessage());
            } catch (SQLException exception) {
                System.out.println("Database error: " + exception.getMessage());
            }
        }
    }

    private void addMachine(int manufacturerId) throws SQLException {
        Machine machine = readMachineDetails(0, manufacturerId);
        int machineId = machineService.addMachine(manufacturerId, machine.getMachineName(), machine.getMachineType());
        System.out.println("Machine added successfully. ID: " + machineId);
    }

    private void searchMachine(int manufacturerId) throws SQLException {
        System.out.print("Enter machine ID, name, or type: ");
        printMachines(machineService.searchMachines(manufacturerId, scanner.nextLine()));
    }

    private void updateMachine(int manufacturerId) throws SQLException {
        int machineId = readPositiveInt("Machine ID to update");
        Machine machine = machineService.getMachineForManufacturer(machineId, manufacturerId);
        printMachineDetails(machine);
        Machine updatedMachine = readMachineDetails(machineId, manufacturerId);
        System.out.println(machineService.updateMachine(machineId, manufacturerId, updatedMachine.getMachineName(),
                updatedMachine.getMachineType()) ? "Machine updated successfully." : "Machine could not be updated.");
    }

    private void changeMachineStatus(int manufacturerId) throws SQLException {
        int machineId = readPositiveInt("Machine ID");
        Machine machine = machineService.getMachineForManufacturer(machineId, manufacturerId);
        printMachineDetails(machine);
        String status = readStatus();
        System.out.println(machineService.changeMachineStatus(machineId, manufacturerId, status)
                ? "Machine status changed successfully." : "Machine status could not be changed.");
    }

    private Machine readMachineDetails(int machineId, int manufacturerId) {
        System.out.print("Machine name: ");
        String name = scanner.nextLine();
        System.out.print("Machine type (optional): ");
        String type = scanner.nextLine();
        return new Machine(machineId, manufacturerId, name, type, null);
    }

    private String readStatus() {
        System.out.println("1. AVAILABLE");
        System.out.println("2. IN_USE");
        System.out.println("3. MAINTENANCE");
        System.out.println("4. INACTIVE");
        System.out.print("New status: ");
        return switch (scanner.nextLine().trim()) {
            case "1" -> "AVAILABLE";
            case "2" -> "IN_USE";
            case "3" -> "MAINTENANCE";
            case "4" -> "INACTIVE";
            default -> throw new IllegalArgumentException("Choose a status from 1 to 4.");
        };
    }

    private int readPositiveInt(String label) {
        System.out.print(label + ": ");
        try {
            int value = Integer.parseInt(scanner.nextLine().trim());
            if (value <= 0) {
                throw new IllegalArgumentException(label + " must be a positive number.");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a whole number.");
        }
    }

    private void printMachines(List<Machine> machines) {
        if (machines.isEmpty()) {
            System.out.println("No machines found.");
            return;
        }
        System.out.printf("%-12s %-28s %-25s %-14s%n", "Machine ID", "Machine Name", "Machine Type", "Status");
        System.out.println("---------------------------------------------------------------------------------");
        for (Machine machine : machines) {
            System.out.printf("%-12d %-28s %-25s %-14s%n", machine.getMachineId(), machine.getMachineName(),
                    displayOptional(machine.getMachineType()), machine.getStatus());
        }
    }

    private void printMachineDetails(Machine machine) {
        System.out.println("Machine ID: " + machine.getMachineId());
        System.out.println("Machine Name: " + machine.getMachineName());
        System.out.println("Machine Type: " + displayOptional(machine.getMachineType()));
        System.out.println("Current Status: " + machine.getStatus());
    }

    private String displayOptional(String value) {
        return value == null ? "" : value;
    }

    private void printMenu() {
        System.out.println("\n--- Machine Management ---");
        System.out.println("1. Add Machine");
        System.out.println("2. View Machines");
        System.out.println("3. Search Machine");
        System.out.println("4. Update Machine");
        System.out.println("5. Change Machine Status");
        System.out.println("0. Back");
        System.out.print("Enter Choice: ");
    }
}
