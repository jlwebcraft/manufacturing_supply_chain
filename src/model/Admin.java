package model;

import java.sql.Timestamp;

/**
 * Admin User subclass.
 * Role: ADMIN
 * Status: ACTIVE
 */
public class Admin extends User {

    public Admin() {
        setRole("ADMIN");
        setStatus("ACTIVE");
    }

    public Admin(int userId, String username, String password, String phoneNo, String pin, Timestamp createdAt) {
        super(userId, username, password, phoneNo, pin, "ADMIN", "ACTIVE", createdAt);
    }

    public Admin(String username, String password, String phoneNo, String pin) {
        super(username, password, phoneNo, pin, "ADMIN", "ACTIVE");
    }

    @Override
    public String toString() {
        return "Admin{" +
                "userId=" + getUserId() +
                ", username='" + getUsername() + '\'' +
                ", phoneNo='" + getPhoneNo() + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
    }
}
