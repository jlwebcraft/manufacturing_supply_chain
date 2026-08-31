package exception;

/**
 * Exception thrown for authentication failures (e.g. invalid credentials, pending status, account inactive/rejected).
 */
public class AuthenticationException extends Exception {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
