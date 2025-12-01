package interview.identity.client;

import interview.identity.config.LegacyEntitlementsProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LegacyEntitlementsClientTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
        void retries_on_5xx_then_succeeds() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("oops"));
        server.enqueue(new MockResponse().setResponseCode(502).setBody("bad gateway"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("PLAN_BASIC, ESPN_PLUS"));

        SleeperCounter sleeper = new SleeperCounter();

        LegacyEntitlementsProperties props = propsForServer();
        props.setMaxAttempts(5);
        props.setInitialBackoff(Duration.ofMillis(10));
        props.setMaxBackoff(Duration.ofMillis(40));
        props.setBackoffMultiplier(2.0);
        props.setJitterRatio(0.0); // deterministic for test

        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
        LegacyEntitlementsClient client = new LegacyEntitlementsClient(props, http, sleeper);

        List<String> entitlements = client.fetchEntitlements("user-123", "US");

        assertEquals(List.of("PLAN_BASIC", "ESPN_PLUS"), entitlements);
        assertEquals(3, server.getRequestCount());
        // backoff should have slept twice (between 1->2 and 2->3)
        assertEquals(2, sleeper.sleeps.size());
        assertEquals(10L, sleeper.sleeps.get(0).toMillis());
        assertEquals(20L, sleeper.sleeps.get(1).toMillis());
    }

    @Test
    void does_not_retry_on_4xx_and_throws() {
        server.enqueue(new MockResponse().setResponseCode(400).setBody("bad request"));

        LegacyEntitlementsProperties props = propsForServer();
        props.setMaxAttempts(5);
        props.setJitterRatio(0.0);

        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
        LegacyEntitlementsClient client = new LegacyEntitlementsClient(props, http, d -> {});

        LegacyEntitlementsException ex =
                assertThrows(LegacyEntitlementsException.class, () -> client.fetchEntitlements("user-123", "US"));

        assertTrue(ex.getMessage().contains("non-retryable"));
        assertEquals(1, server.getRequestCount());
    }

    @Test
    void csv_parsing_trims_and_ignores_empty_tokens() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("A, B, ,C ,,  "));

        LegacyEntitlementsProperties props = propsForServer();
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
        LegacyEntitlementsClient client = new LegacyEntitlementsClient(props, http, d -> {});

        List<String> entitlements = client.fetchEntitlements("user-123", "US");
        assertEquals(List.of("A", "B", "C"), entitlements);
    }

    @Test
    void metrics_increment_success_and_failure() {
        // success path
        server.enqueue(new MockResponse().setResponseCode(200).setBody("A,B"));

        LegacyEntitlementsProperties props = propsForServer();
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        LegacyEntitlementsClient client = new LegacyEntitlementsClient(props, http, d -> {}, registry);

        assertEquals(List.of("A", "B"), client.fetchEntitlements("user-123", "US"));

        assertEquals(1.0, registry.get("legacy_entitlements_success_total").counter().count());
        assertEquals(0.0, registry.get("legacy_entitlements_failure_total").counter().count());
        assertTrue(registry.get("legacy_entitlements_call_seconds").timer().count() >= 1);

        // failure path (non-retryable)
        server.enqueue(new MockResponse().setResponseCode(400).setBody("bad request"));

        assertThrows(LegacyEntitlementsException.class, () -> client.fetchEntitlements("user-123", "US"));
        assertEquals(1.0, registry.get("legacy_entitlements_failure_total").counter().count());
    }

    private LegacyEntitlementsProperties propsForServer() {
        LegacyEntitlementsProperties props = new LegacyEntitlementsProperties();
        props.setBaseUrl(server.url("/legacy/entitlements").toString());
        props.setConnectTimeout(Duration.ofSeconds(1));
        props.setRequestTimeout(Duration.ofMillis(200));
        props.setMaxAttempts(3);
        props.setInitialBackoff(Duration.ofMillis(10));
        props.setMaxBackoff(Duration.ofMillis(40));
        props.setBackoffMultiplier(2.0);
        props.setJitterRatio(0.0);
        return props;
    }

    private static class SleeperCounter implements Sleeper {
        final List<Duration> sleeps = new ArrayList<>();
        @Override public void sleep(Duration duration) { sleeps.add(duration); }
    }
}
