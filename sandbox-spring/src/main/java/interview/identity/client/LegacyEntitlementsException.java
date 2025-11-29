package interview.identity.client;

public class LegacyEntitlementsException extends RuntimeException {
    public LegacyEntitlementsException(String message) {
        super(message);
    }

    public LegacyEntitlementsException(String message, Throwable cause) {
        super(message, cause);
    }
}
