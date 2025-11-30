package interview.identity.client;

import interview.identity.config.LegacyEntitlementsProperties;
import interview.identity.exception.LegacyEntitlementsUnavailableException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LegacyEntitlementsClient implements EntitlementsClient {
    private static final Logger log = LoggerFactory.getLogger(LegacyEntitlementsClient.class);

    private final LegacyEntitlementsProperties props;
    private final HttpClient http;
    private final Sleeper sleeper;

    public LegacyEntitlementsClient() {
        // Default constructor for simplicity in this snippet; in your app you can inject props via Spring.
        this(new LegacyEntitlementsProperties());
    }

    public LegacyEntitlementsClient(LegacyEntitlementsProperties props) {
        this(props,
                HttpClient.newBuilder()
                        .connectTimeout(props.getConnectTimeout())
                        .build(),
                Sleeper.system());
    }

    // Package-private for tests
    LegacyEntitlementsClient(LegacyEntitlementsProperties props, HttpClient http, Sleeper sleeper) {
        this.props = props;
        this.http = http;
        this.sleeper = sleeper;
    }

    @Override
    public List<String> fetchEntitlements(String userId, String region) {
        validateInputs(userId, region);

        int attempts = 0;
        Exception lastError = null;

        while (attempts < props.getMaxAttempts()) {
            attempts++;

            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(buildUri(userId, region))
                        .timeout(props.getRequestTimeout())
                        .GET()
                        .build();

                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                int status = resp.statusCode();

                if (status >= 200 && status < 300) {
                    return parseCsv(resp.body());
                }

                // Don't log body (could contain PII). Log status + attempt.
                log.warn("Legacy entitlements call failed status={} attempt={}/{}", status, attempts, props.getMaxAttempts());

                if (!isRetryableStatus(status)) {
                    throw new LegacyEntitlementsException("Legacy returned non-retryable status: " + status);
                }

            } catch (IOException e) {
                lastError = e;
                log.warn("Legacy entitlements IO failure attempt={}/{} err={}", attempts, props.getMaxAttempts(), e.toString());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LegacyEntitlementsException("Interrupted while calling legacy entitlements", e);
            } catch (LegacyEntitlementsException e) {
                // non-retryable or explicit failure
                throw e;
            } catch (Exception e) {
                // unexpected, treat as non-retryable by default
                throw new LegacyEntitlementsException("Unexpected error calling legacy entitlements", e);
            }

            // Backoff before next retry (unless that was the last attempt)
            if (attempts < props.getMaxAttempts()) {
                try {
                    sleeper.sleep(computeBackoff(attempts));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LegacyEntitlementsException("Interrupted during retry backoff", ie);
                }
            }
        }

        throw new LegacyEntitlementsUnavailableException(
                "Legacy entitlements failed after retries",
                lastError
        );
    }

    private URI buildUri(String userId, String region) {
        String url = props.getBaseUrl()
                + "?userId=" + URLEncoder.encode(userId, StandardCharsets.UTF_8)
                + "&region=" + URLEncoder.encode(region, StandardCharsets.UTF_8);
        return URI.create(url);
    }

    private boolean isRetryableStatus(int status) {
        // Retry on 5xx and 429 (throttled). Not on 4xx generally.
        return status >= 500 || status == 429;
    }

    private Duration computeBackoff(int attemptNumber) {
        // attemptNumber starts at 1; backoff for attempt 2 is initialBackoff * multiplier^(attempt-2)
        int exponent = Math.max(0, attemptNumber - 1);
        double baseMillis = props.getInitialBackoff().toMillis() * Math.pow(props.getBackoffMultiplier(), exponent);
        long capped = Math.min((long) baseMillis, props.getMaxBackoff().toMillis());

        double jitter = props.getJitterRatio();
        if (jitter <= 0.0) return Duration.ofMillis(Math.max(0L, capped));

        // jitter in range [-jitter, +jitter]
        double factor = 1.0 + ThreadLocalRandom.current().nextDouble(-jitter, jitter);
        long withJitter = (long) (capped * factor);

        return Duration.ofMillis(Math.max(0L, withJitter));
    }

    private void validateInputs(String userId, String region) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId required");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("region required");
        }
    }

    // Improved parsing: trims and ignores empty tokens
    private List<String> parseCsv(String body) {
        List<String> out = new ArrayList<>();
        if (body == null || body.isBlank()) return out;

        for (String raw : body.split(",")) {
            String s = raw == null ? "" : raw.trim();
            if (!s.isEmpty()) out.add(s);
        }

        return out;
    }
}
