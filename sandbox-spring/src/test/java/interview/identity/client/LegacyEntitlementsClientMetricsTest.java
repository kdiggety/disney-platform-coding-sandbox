package interview.identity.client;

import interview.identity.config.LegacyEntitlementsProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.net.http.HttpClient;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LegacyEntitlementsClientMetricsTest {

    @Test
    void incrementsFailureCounter_whenRetriesExhausted() {
        LegacyEntitlementsProperties props = new LegacyEntitlementsProperties();
        props.setMaxAttempts(1);

        HttpClient http = mock(HttpClient.class); // your existing tests likely stub send(); here we just trigger failure differently
        Sleeper sleeper = duration -> {}; // no-op

        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        LegacyEntitlementsClient client = new LegacyEntitlementsClient(props, http, sleeper, registry);

        assertThrows(RuntimeException.class, () -> client.fetchEntitlements("user-123", "US"));

        double failures = registry.get("legacy_entitlements_failure_total").counter().count();
        // will be 1.0 if you hit terminal failure path
        // (if your mock setup throws earlier, this still works as long as it reaches a failure path)
        assertTrue(failures >= 1.0);
    }
}
