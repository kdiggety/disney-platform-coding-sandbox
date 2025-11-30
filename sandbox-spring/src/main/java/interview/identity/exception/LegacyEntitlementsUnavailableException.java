package interview.identity.exception;

public class LegacyEntitlementsUnavailableException extends RuntimeException {
    public LegacyEntitlementsUnavailableException(String message) { super(message); }
    public LegacyEntitlementsUnavailableException(String message, Throwable cause) { super(message, cause); }
}
