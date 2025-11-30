package interview.identity.client;

import interview.identity.exception.LegacyEntitlementsUnavailableException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class ResilientEntitlementsClient implements EntitlementsClient {

    private final LegacyEntitlementsClient delegate;

    public ResilientEntitlementsClient(LegacyEntitlementsClient delegate) {
        this.delegate = delegate;
    }

    @Override
    @CircuitBreaker(name = "legacyEntitlements", fallbackMethod = "fallback")
    @Bulkhead(name = "legacyEntitlements")
    public List<String> fetchEntitlements(String userId, String region) {
        return delegate.fetchEntitlements(userId, region);
    }

    @SuppressWarnings("unused")
    private List<String> fallback(String userId, String region, Throwable t) {
        // IMPORTANT: keep error mapping stable (Step 1)
        throw new LegacyEntitlementsUnavailableException("Legacy entitlements unavailable", t);
    }
}
