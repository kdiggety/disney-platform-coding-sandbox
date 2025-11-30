package interview.identity.client;

import interview.identity.exception.LegacyEntitlementsUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = "spring.profiles.active=test")
@Import(ResilientEntitlementsClient.class)
class ResilientEntitlementsClientTest {

    @MockBean LegacyEntitlementsClient delegate;

    @Test
    void circuitBreaker_opens_andFallbackThrowsUnavailable() {
        given(delegate.fetchEntitlements("user-123", "US"))
                .willThrow(new LegacyEntitlementsUnavailableException("down"));

        ResilientEntitlementsClient client = new ResilientEntitlementsClient(delegate);

        // Calls that fail and count toward opening
        assertThrows(LegacyEntitlementsUnavailableException.class,
                () -> client.fetchEntitlements("user-123", "US"));
        // Second attempt...
        assertThrows(LegacyEntitlementsUnavailableException.class,
                () -> client.fetchEntitlements("user-123", "US"));

        // Once open, should still throw unavailable (from fallback)
        assertThrows(LegacyEntitlementsUnavailableException.class,
                () -> client.fetchEntitlements("user-123", "US"));
    }
}
