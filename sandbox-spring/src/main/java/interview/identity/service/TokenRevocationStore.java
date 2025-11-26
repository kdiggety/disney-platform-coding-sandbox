package interview.identity.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TokenRevocationStore {
    // Intentionally flawed: not thread-safe, no TTL/cleanup
    private final Map<String, String> revoked = new HashMap<>();

    public void revoke(String token, String reason) {
        revoked.put(token, reason == null ? "" : reason);
    }

    public boolean isRevoked(String token) {
        return revoked.containsKey(token);
    }
}
