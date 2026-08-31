package util;

import exception.InvalidInputException;

/**
 * Input validation utility to sanitize and validate user input before processing.
 */
public class InputValidator {

    private InputValidator() {
        // Utility class
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static void validateRequired(String input, String fieldName) throws InvalidInputException {
        if (isNullOrEmpty(input)) {
            throw new InvalidInputException(fieldName + " cannot be empty.");
        }
    }

    public static void validateUsername(String username) throws InvalidInputException {
        validateRequired(username, "Username");
        String trimmed = username.trim();
        if (trimmed.length() < 3) {
            throw new InvalidInputException("Username must be at least 3 characters long.");
        }
        if (!trimmed.matches("^[a-zA-Z0-9_]+$")) {
            throw new InvalidInputException("Username can only contain alphanumeric characters and underscores.");
        }
    }

    public static void validatePassword(String password) throws InvalidInputException {
        validateRequired(password, "Password");
        if (password.length() < 4) {
            throw new InvalidInputException("Password must be at least 4 characters long.");
        }
    }

    public static void validatePhone(String phoneNo) throws InvalidInputException {
        validateRequired(phoneNo, "Phone Number");
        String trimmed = phoneNo.trim();
        if (!trimmed.matches("^[0-9]{10,15}$")) {
            throw new InvalidInputException("Phone number must contain between 10 and 15 numeric digits.");
        }
    }

    public static void validatePin(String pin) throws InvalidInputException {
        validateRequired(pin, "PIN");
        String trimmed = pin.trim();
        if (!trimmed.matches("^[0-9]{4,6}$")) {
            throw new InvalidInputException("PIN must be a 4 to 6 digit numeric code.");
        }
    }

    public static int validatePositiveInt(String input, String fieldName) throws InvalidInputException {
        validateRequired(input, fieldName);
        try {
            int value = Integer.parseInt(input.trim());
            if (value <= 0) {
                throw new InvalidInputException(fieldName + " must be a positive integer greater than zero.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new InvalidInputException(fieldName + " must be a valid numeric integer.");
        }
    }
}
