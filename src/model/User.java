package model;

import java.sql.Timestamp;

/**
 * Base User class for all user types in the system.
 * Table: users
 */
public class User {

    private int userId;
    private String username;
    private String password;
    private String phoneNo;
    private String pin;
    private String role;
    private String status;
    private Timestamp createdAt;

    public User() {
    }

    public User(int userId, String username, String password, String phoneNo, String pin, String role, String status, Timestamp createdAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.phoneNo = phoneNo;
        this.pin = pin;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public User(String username, String password, String phoneNo, String pin, String role, String status) {
        this.username = username;
        this.password = password;
        this.phoneNo = phoneNo;
        this.pin = pin;
        this.role = role;
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", phoneNo='" + phoneNo + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
