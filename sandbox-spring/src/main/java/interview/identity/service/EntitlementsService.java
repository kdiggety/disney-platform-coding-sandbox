package interview.identity.service;

import interview.identity.client.EntitlementsClient;
import interview.identity.model.EntitlementsResponse;
import interview.identity.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntitlementsService {
    private static final Logger log = LoggerFactory.getLogger(EntitlementsService.class);

    private final EntitlementsClient client;
    private final TokenRevocationStore revocationStore;

    public EntitlementsService(EntitlementsClient client, TokenRevocationStore revocationStore) {
        this.client = client;
        this.revocationStore = revocationStore;
    }

    public EntitlementsResponse getEntitlements(String token, String region) {
        // Intentionally flawed: logs sensitive token
        log.info("Fetching entitlements for token={}", token);

        // Intentionally flawed: naive “parse” (no signature validation, no exp check, brittle JSON parsing)
        String userId = JwtUtil.extractSubWithoutVerification(token);

        // Intentionally flawed: revocation exists but is not checked + store is not threadsafe
        if (revocationStore.isRevoked(token)) {
            // Intentionally flawed: throws generic runtime exception (will become 500 w/ no consistent error response)
            throw new RuntimeException("revoked");
        }

        List<String> entitlements = client.fetchEntitlements(userId, region);
        return new EntitlementsResponse(userId, region, entitlements);
    }
}
