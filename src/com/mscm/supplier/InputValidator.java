package com.mscm.supplier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.regex.Pattern;

/** All terminal input passes through this class so invalid input never terminates the program. */
public final class InputValidator {
    private static final Pattern NAME = Pattern.compile("[A-Za-z][A-Za-z .,'&()-]{0,99}");
    private static final Pattern PHONE = Pattern.compile("\\d{10,15}");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private InputValidator() { }

    public static int menuChoice(Scanner scanner, int minimum, int maximum) {
        while (true) {
            String text = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(text);
                if (choice >= minimum && choice <= maximum) return choice;
            } catch (NumberFormatException ignored) { }
            System.out.print("Enter a number from " + minimum + " to " + maximum + ": ");
        }
    }

    public static int positiveInt(Scanner scanner, String label, boolean allowBack) {
        while (true) {
            System.out.print(label + (allowBack ? " (0 to go back): " : ": "));
            String text = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(text);
                if (allowBack && value == 0) return 0;
                if (value > 0) return value;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a positive whole number.");
        }
    }

    public static String requiredText(Scanner scanner, String label, boolean nameOnly) {
        while (true) {
            System.out.print(label + ": ");
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                System.out.println("This field cannot be blank.");
            } else if (nameOnly && !NAME.matcher(value).matches()) {
                System.out.println("Use letters, spaces, and normal name punctuation only.");
            } else {
                return value;
            }
        }
    }

    public static String optionalText(Scanner scanner, String label) {
        System.out.print(label + " (press Enter to keep current value): ");
        return scanner.nextLine().trim();
    }

    public static String phone(Scanner scanner, String label) {
        while (true) {
            System.out.print(label + ": ");
            String value = scanner.nextLine().trim();
            if (PHONE.matcher(value).matches()) return value;
            System.out.println("Phone number must contain 10 to 15 digits only.");
        }
    }

    public static LocalDate futureOrTodayDate(Scanner scanner, String label) {
        while (true) {
            System.out.print(label + " (yyyy-MM-dd): ");
            try {
                LocalDate date = LocalDate.parse(scanner.nextLine().trim(), DATE_FORMAT);
                if (!date.isBefore(LocalDate.now())) return date;
                System.out.println("Date cannot be in the past.");
            } catch (DateTimeParseException ex) {
                System.out.println("Use the date format yyyy-MM-dd.");
            }
        }
    }

    public static BigDecimal positiveMoney(Scanner scanner, String label) {
        while (true) {
            System.out.print(label + ": ");
            try {
                BigDecimal value = new BigDecimal(scanner.nextLine().trim());
                if (value.signum() > 0 && value.scale() <= 2) return value;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a positive amount with at most two decimal places.");
        }
    }

    public static String priority(Scanner scanner) {
        while (true) {
            System.out.print("Priority (LOW/NORMAL/HIGH/URGENT): ");
            String value = scanner.nextLine().trim().toUpperCase();
            if (value.equals("LOW") || value.equals("NORMAL") || value.equals("HIGH") || value.equals("URGENT")) return value;
            System.out.println("Enter LOW, NORMAL, HIGH, or URGENT.");
        }
    }
}
