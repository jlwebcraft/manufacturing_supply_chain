package util;

import java.util.Scanner;

public final class InputUtil {
    private static final InputUtil INSTANCE = new InputUtil();
    private final Scanner scanner;

    private InputUtil() {
        scanner = new Scanner(System.in);
    }

    public static InputUtil getInstance() {
        return INSTANCE;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();

            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
