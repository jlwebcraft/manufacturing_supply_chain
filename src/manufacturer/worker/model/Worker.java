package manufacturer.worker.model;

/** Represents a worker record from the existing workers table. */
public class Worker {
    private int workerId;
    private int manufacturerId;
    private String workerName;
    private String skill;
    private String contactNo;
    private String status;

    public Worker() {
    }

    public Worker(int manufacturerId, String workerName, String skill, String contactNo) {
        this.manufacturerId = manufacturerId;
        this.workerName = workerName;
        this.skill = skill;
        this.contactNo = contactNo;
    }

    public Worker(int workerId, int manufacturerId, String workerName, String skill, String contactNo, String status) {
        this.workerId = workerId;
        this.manufacturerId = manufacturerId;
        this.workerName = workerName;
        this.skill = skill;
        this.contactNo = contactNo;
        this.status = status;
    }

    public int getWorkerId() { return workerId; }
    public void setWorkerId(int workerId) { this.workerId = workerId; }
    public int getManufacturerId() { return manufacturerId; }
    public void setManufacturerId(int manufacturerId) { this.manufacturerId = manufacturerId; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
