package manufacturer.worker.menu;

import manufacturer.worker.model.Worker;
import manufacturer.worker.service.WorkerService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/** Reusable terminal menu for workers belonging to a logged-in manufacturer. */
public class WorkerMenu {
    private final WorkerService workerService = new WorkerService();
    private final Scanner scanner = new Scanner(System.in);

    public void showMenu(int manufacturerId) {
        try {
            workerService.validateManufacturer(manufacturerId);
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
                    case "1": addWorker(manufacturerId); break;
                    case "2": printWorkers(workerService.viewWorkers(manufacturerId)); break;
                    case "3": searchWorker(manufacturerId); break;
                    case "4": updateWorker(manufacturerId); break;
                    case "5": changeWorkerStatus(manufacturerId); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice. Please choose 0 to 5.");
                }
            } catch (IllegalArgumentException exception) {
                System.out.println("Input error: " + exception.getMessage());
            } catch (SQLException exception) {
                System.out.println("Database error: " + exception.getMessage());
            }
        }
    }

    private void addWorker(int manufacturerId) throws SQLException {
        Worker worker = readWorkerDetails(0, manufacturerId);
        int workerId = workerService.addWorker(manufacturerId, worker.getWorkerName(), worker.getSkill(),
                worker.getContactNo());
        System.out.println("Worker added successfully. ID: " + workerId);
    }

    private void searchWorker(int manufacturerId) throws SQLException {
        System.out.print("Enter worker ID, name, skill, or contact number: ");
        printWorkers(workerService.searchWorkers(manufacturerId, scanner.nextLine()));
    }

    private void updateWorker(int manufacturerId) throws SQLException {
        int workerId = readPositiveInt("Worker ID to update");
        Worker worker = workerService.getWorkerForManufacturer(workerId, manufacturerId);
        printWorkerDetails(worker);
        Worker updatedWorker = readWorkerDetails(workerId, manufacturerId);
        System.out.println(workerService.updateWorker(workerId, manufacturerId, updatedWorker.getWorkerName(),
                updatedWorker.getSkill(), updatedWorker.getContactNo())
                ? "Worker updated successfully." : "Worker could not be updated.");
    }

    private void changeWorkerStatus(int manufacturerId) throws SQLException {
        int workerId = readPositiveInt("Worker ID");
        Worker worker = workerService.getWorkerForManufacturer(workerId, manufacturerId);
        printWorkerDetails(worker);
        String status = readStatus();
        System.out.println(workerService.changeWorkerStatus(workerId, manufacturerId, status)
                ? "Worker status changed successfully." : "Worker status could not be changed.");
    }

    private Worker readWorkerDetails(int workerId, int manufacturerId) {
        System.out.print("Worker name: ");
        String name = scanner.nextLine();
        System.out.print("Skill (optional): ");
        String skill = scanner.nextLine();
        System.out.print("Contact number (optional): ");
        String contactNo = scanner.nextLine();
        return new Worker(workerId, manufacturerId, name, skill, contactNo, null);
    }

    private String readStatus() {
        System.out.println("1. AVAILABLE");
        System.out.println("2. ASSIGNED");
        System.out.println("3. ON_LEAVE");
        System.out.println("4. INACTIVE");
        System.out.print("New status: ");
        switch (scanner.nextLine().trim()) {
            case "1": return "AVAILABLE";
            case "2": return "ASSIGNED";
            case "3": return "ON_LEAVE";
            case "4": return "INACTIVE";
            default: throw new IllegalArgumentException("Choose a status from 1 to 4.");
        }
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

    private void printWorkers(List<Worker> workers) {
        if (workers.isEmpty()) {
            System.out.println("No workers found.");
            return;
        }
        System.out.printf("%-10s %-25s %-22s %-18s %-12s%n", "Worker ID", "Worker Name", "Skill",
                "Contact No.", "Status");
        System.out.println("------------------------------------------------------------------------------------------");
        for (Worker worker : workers) {
            System.out.printf("%-10d %-25s %-22s %-18s %-12s%n", worker.getWorkerId(), worker.getWorkerName(),
                    displayOptional(worker.getSkill()), displayOptional(worker.getContactNo()), worker.getStatus());
        }
    }

    private void printWorkerDetails(Worker worker) {
        System.out.println("Worker ID: " + worker.getWorkerId());
        System.out.println("Worker Name: " + worker.getWorkerName());
        System.out.println("Skill: " + displayOptional(worker.getSkill()));
        System.out.println("Contact No.: " + displayOptional(worker.getContactNo()));
        System.out.println("Current Status: " + worker.getStatus());
    }

    private String displayOptional(String value) {
        return value == null ? "" : value;
    }

    private void printMenu() {
        System.out.println("\n--- Worker Management ---");
        System.out.println("1. Add Worker");
        System.out.println("2. View Workers");
        System.out.println("3. Search Worker");
        System.out.println("4. Update Worker");
        System.out.println("5. Change Worker Status");
        System.out.println("0. Back");
        System.out.print("Enter Choice: ");
    }
}
