package manufacturer.machine.model;

/** Represents a machine record from the existing machines table. */
public class Machine {
    private int machineId;
    private int manufacturerId;
    private String machineName;
    private String machineType;
    private String status;

    public Machine() {
    }

    public Machine(int manufacturerId, String machineName, String machineType) {
        this.manufacturerId = manufacturerId;
        this.machineName = machineName;
        this.machineType = machineType;
    }

    public Machine(int machineId, int manufacturerId, String machineName, String machineType, String status) {
        this.machineId = machineId;
        this.manufacturerId = manufacturerId;
        this.machineName = machineName;
        this.machineType = machineType;
        this.status = status;
    }

    public int getMachineId() { return machineId; }
    public void setMachineId(int machineId) { this.machineId = machineId; }
    public int getManufacturerId() { return manufacturerId; }
    public void setManufacturerId(int manufacturerId) { this.manufacturerId = manufacturerId; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public String getMachineType() { return machineType; }
    public void setMachineType(String machineType) { this.machineType = machineType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
