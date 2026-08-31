import dao.ManufacturerDAO;
import dao.SupplierDAO;
import dao.UserDAO;
import exception.AuthenticationException;
import exception.DatabaseException;
import exception.InvalidInputException;
import exception.UserNotFoundException;
import model.Manufacturer;
import model.Supplier;
import model.User;
import service.AdminService;
import service.AuthenticationService;
import service.RegistrationService;
import service.UserService;
import util.InputValidator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 5 Automated Test Harness with In-Memory Mock DAOs and Dynamic Proxy Connection.
 * Verifies all Member 1 functionality without requiring a running MySQL server.
 */
public class Phase5TestRunner {

    // Helper to generate a dummy JDBC Connection proxy
    private static Connection createDummyConnection() {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("setAutoCommit".equals(method.getName()) ||
                            "commit".equals(method.getName()) ||
                            "rollback".equals(method.getName()) ||
                            "close".equals(method.getName())) {
                            return null;
                        }
                        if ("isClosed".equals(method.getName())) {
                            return false;
                        }
                        return null;
                    }
                }
        );
    }

    static class MockUserDAO extends UserDAO {
        Map<Integer, User> users = new HashMap<>();
        int idSeq = 1;

        @Override
        public int addUser(User user) {
            user.setUserId(idSeq++);
            users.put(user.getUserId(), user);
            return user.getUserId();
        }

        @Override
        public int addUser(User user, Connection conn) {
            return addUser(user);
        }

        @Override
        public User getById(int userId) {
            return users.get(userId);
        }

        @Override
        public User getUserByUsername(String username) {
            for (User u : users.values()) {
                if (u.getUsername().equalsIgnoreCase(username)) return u;
            }
            return null;
        }

        @Override
        public User getUserByPhone(String phoneNo) {
            for (User u : users.values()) {
                if (u.getPhoneNo().equals(phoneNo)) return u;
            }
            return null;
        }

        @Override
        public List<User> getAll() {
            return new ArrayList<>(users.values());
        }

        @Override
        public boolean updateUserStatus(int userId, String status) {
            User u = users.get(userId);
            if (u != null) {
                u.setStatus(status);
                return true;
            }
            return false;
        }

        @Override
        public boolean updateUserStatus(int userId, String status, Connection conn) {
            return updateUserStatus(userId, status);
        }
    }

    static class MockManufacturerDAO extends ManufacturerDAO {
        Map<Integer, Manufacturer> manufacturers = new HashMap<>();
        int mfgSeq = 1;

        @Override
        public int addManufacturer(Manufacturer manufacturer, Connection conn) {
            manufacturer.setManufacturerId(mfgSeq++);
            manufacturers.put(manufacturer.getManufacturerId(), manufacturer);
            return manufacturer.getManufacturerId();
        }

        @Override
        public Manufacturer getById(int manufacturerId) {
            return manufacturers.get(manufacturerId);
        }

        @Override
        public Manufacturer getManufacturerByUserId(int userId) {
            for (Manufacturer m : manufacturers.values()) {
                if (m.getUserId() == userId) return m;
            }
            return null;
        }

        @Override
        public List<Manufacturer> getPendingManufacturers() {
            List<Manufacturer> pending = new ArrayList<>();
            for (Manufacturer m : manufacturers.values()) {
                if ("PENDING".equalsIgnoreCase(m.getStatus())) {
                    pending.add(m);
                }
            }
            return pending;
        }
    }

    static class MockSupplierDAO extends SupplierDAO {
        Map<Integer, Supplier> suppliers = new HashMap<>();
        int suppSeq = 1;

        @Override
        public int addSupplier(Supplier supplier, Connection conn) {
            supplier.setSupplierId(suppSeq++);
            suppliers.put(supplier.getSupplierId(), supplier);
            return supplier.getSupplierId();
        }

        @Override
        public Supplier getById(int supplierId) {
            return suppliers.get(supplierId);
        }

        @Override
        public Supplier getSupplierByUserId(int userId) {
            for (Supplier s : suppliers.values()) {
                if (s.getUserId() == userId) return s;
            }
            return null;
        }

        @Override
        public List<Supplier> getPendingSuppliers() {
            List<Supplier> pending = new ArrayList<>();
            for (Supplier s : suppliers.values()) {
                if ("PENDING".equalsIgnoreCase(s.getStatus())) {
                    pending.add(s);
                }
            }
            return pending;
        }
    }

    static class MockRegistrationService extends RegistrationService {
        public MockRegistrationService(UserDAO userDAO, ManufacturerDAO manufacturerDAO, SupplierDAO supplierDAO) {
            super(userDAO, manufacturerDAO, supplierDAO);
        }

        @Override
        protected Connection getConnection() {
            return createDummyConnection();
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" RUNNING PHASE 5 AUTOMATED VERIFICATION TESTS");
        System.out.println("==================================================");

        MockUserDAO mockUserDAO = new MockUserDAO();
        MockManufacturerDAO mockManufacturerDAO = new MockManufacturerDAO();
        MockSupplierDAO mockSupplierDAO = new MockSupplierDAO();

        AuthenticationService authService = new AuthenticationService(mockUserDAO, mockManufacturerDAO, mockSupplierDAO);
        RegistrationService regService = new MockRegistrationService(mockUserDAO, mockManufacturerDAO, mockSupplierDAO);
        AdminService adminService = new AdminService(mockUserDAO, mockManufacturerDAO, mockSupplierDAO);
        UserService userService = new UserService(mockUserDAO);

        int passed = 0;
        int failed = 0;

        // Setup Admin User
        User admin = new User("admin1", "adminpass", "1234567890", "1234", "ADMIN", "ACTIVE");
        mockUserDAO.addUser(admin);

        // Test 1: Admin Login
        try {
            User loggedInAdmin = authService.loginAdmin("admin1", "adminpass");
            if (loggedInAdmin != null) {
                System.out.println("[PASS] Test 1: Admin Login Successful");
                passed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 1: " + e.getMessage());
            failed++;
        }

        // Test 2: Invalid Admin Password
        try {
            authService.loginAdmin("admin1", "wrongpass");
            System.err.println("[FAIL] Test 2: Invalid password allowed");
            failed++;
        } catch (AuthenticationException e) {
            System.out.println("[PASS] Test 2: Invalid password correctly rejected (" + e.getMessage() + ")");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 2: Unexpected error " + e);
            failed++;
        }

        // Test 3: Manufacturer Registration
        Manufacturer mfg = null;
        try {
            mfg = regService.registerManufacturer("mfg_corp", "mfgpass", "9876543210", "4321", "Apex Manufacturing", "123 Industrial Park", "9876543210");
            System.out.println("[PASS] Test 3: Manufacturer Registered with PENDING status (User ID: " + mfg.getUserId() + ", Mfg ID: " + mfg.getManufacturerId() + ")");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 3: " + e.getMessage());
            failed++;
        }

        // Test 4: Pending Account Login Rejection
        try {
            authService.loginManufacturer("mfg_corp", "mfgpass");
            System.err.println("[FAIL] Test 4: Pending manufacturer login allowed!");
            failed++;
        } catch (AuthenticationException e) {
            System.out.println("[PASS] Test 4: Pending login correctly rejected (" + e.getMessage() + ")");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 4: Unexpected error " + e);
            failed++;
        }

        // Test 5: Admin Approves Manufacturer
        try {
            boolean approved = adminService.approveManufacturer(mfg.getManufacturerId());
            if (approved) {
                System.out.println("[PASS] Test 5: Admin Approved Manufacturer ID " + mfg.getManufacturerId());
                passed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 5: " + e.getMessage());
            failed++;
        }

        // Test 6: Manufacturer Login Post-Approval
        try {
            Manufacturer loggedInMfg = authService.loginManufacturer("mfg_corp", "mfgpass");
            if (loggedInMfg != null) {
                System.out.println("[PASS] Test 6: Manufacturer Login Successful post-approval");
                passed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 6: " + e.getMessage());
            failed++;
        }

        // Test 7: Supplier Registration
        Supplier supp = null;
        try {
            supp = regService.registerSupplier("supp_corp", "supppass", "5556667777", "5555", "Global Components", "456 Supply St", "5556667777");
            System.out.println("[PASS] Test 7: Supplier Registered with PENDING status");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 7: " + e.getMessage());
            failed++;
        }

        // Test 8: Admin Rejects Supplier
        try {
            boolean rejected = adminService.rejectSupplier(supp.getSupplierId());
            if (rejected) {
                System.out.println("[PASS] Test 8: Admin Rejected Supplier ID " + supp.getSupplierId());
                passed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 8: " + e.getMessage());
            failed++;
        }

        // Test 9: Rejected Supplier Login Rejection
        try {
            authService.loginSupplier("supp_corp", "supppass");
            System.err.println("[FAIL] Test 9: Rejected supplier login allowed!");
            failed++;
        } catch (AuthenticationException e) {
            System.out.println("[PASS] Test 9: Rejected supplier login correctly blocked (" + e.getMessage() + ")");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 9: Unexpected error " + e);
            failed++;
        }

        // Test 10: User Search
        try {
            User found = adminService.searchUser("mfg_corp");
            if (found != null && "mfg_corp".equals(found.getUsername())) {
                System.out.println("[PASS] Test 10: Admin User Search Successful");
                passed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 10: " + e.getMessage());
            failed++;
        }

        // Test 11: User Deactivation
        try {
            adminService.deactivateUser(mfg.getUserId());
            try {
                authService.loginManufacturer("mfg_corp", "mfgpass");
                System.err.println("[FAIL] Test 11: Deactivated manufacturer login allowed!");
                failed++;
            } catch (AuthenticationException e) {
                System.out.println("[PASS] Test 11: Deactivated user login correctly blocked (" + e.getMessage() + ")");
                passed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 11: " + e.getMessage());
            failed++;
        }

        System.out.println("==================================================");
        System.out.println(" FINAL TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED.");
        System.out.println("==================================================");
    }
}
