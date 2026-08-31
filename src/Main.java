import controller.LoginController;

/**
 * Main application entry point for Manufacturing and Supply Chain Management System.
 */
public class Main {
    public static void main(String[] args) {
        LoginController loginController = new LoginController();
        loginController.startMenu();
    }
}