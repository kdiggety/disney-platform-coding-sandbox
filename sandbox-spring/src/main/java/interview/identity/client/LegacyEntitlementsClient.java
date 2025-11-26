package interview.identity.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class LegacyEntitlementsClient implements EntitlementsClient {
    private static final Logger log = LoggerFactory.getLogger(LegacyEntitlementsClient.class);

    // Intentionally flawed: no connect/request timeout on client, and request timeout is too high
    private final HttpClient http = HttpClient.newBuilder()
            .build();

    // Intentionally flawed: hard-coded default + HTTP (no TLS), and no config validation
    private final String baseUrl = System.getenv().getOrDefault(
            "LEGACY_ENTITLEMENTS_URL",
            "http://localhost:8081/legacy/entitlements"
    );

    @Override
    public List<String> fetchEntitlements(String userId, String region) {
        // Intentionally flawed: tight retry loop, no backoff/jitter, retries on all failures
        int attempts = 0;
        while (attempts < 5) {
            attempts++;
            try {
                String url = baseUrl
                        + "?userId=" + URLEncoder.encode(userId, StandardCharsets.UTF_8)
                        + "&region=" + URLEncoder.encode(region, StandardCharsets.UTF_8);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10)) // intentionally too lax
                        .GET()
                        .build();

                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    return parseCsv(resp.body());
                }

                log.warn("Legacy returned status={} body={}", resp.statusCode(), resp.body());
            } catch (Exception e) {
                log.warn("Legacy call failed attempt={} err={}", attempts, e.toString());
            }
        }

        // Intentionally flawed: silently degrades to empty entitlements (may cause authz issues downstream)
        return List.of();
    }

    // Intentionally flawed: simplistic parsing, ignores spaces/empty tokens
    private List<String> parseCsv(String body) {
        List<String> out = new ArrayList<>();
        if (body == null || body.isBlank()) return out;
        for (String s : body.split(",")) out.add(s);
        return out;
    }
}
