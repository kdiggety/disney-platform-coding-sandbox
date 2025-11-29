package interview.identity.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class LegacyEntitlementsProperties {
    private String baseUrl = "http://localhost:8081/legacy/entitlements";

    private Duration connectTimeout = Duration.ofSeconds(2);
    private Duration requestTimeout = Duration.ofSeconds(2);

    private int maxAttempts = 3;

    private Duration initialBackoff = Duration.ofMillis(100);
    private Duration maxBackoff = Duration.ofSeconds(2);
    private double backoffMultiplier = 2.0;

    /** 0.2 = +/- 20% jitter */
    private double jitterRatio = 0.2;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }

    public Duration getRequestTimeout() { return requestTimeout; }
    public void setRequestTimeout(Duration requestTimeout) { this.requestTimeout = requestTimeout; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public Duration getInitialBackoff() { return initialBackoff; }
    public void setInitialBackoff(Duration initialBackoff) { this.initialBackoff = initialBackoff; }

    public Duration getMaxBackoff() { return maxBackoff; }
    public void setMaxBackoff(Duration maxBackoff) { this.maxBackoff = maxBackoff; }

    public double getBackoffMultiplier() { return backoffMultiplier; }
    public void setBackoffMultiplier(double backoffMultiplier) { this.backoffMultiplier = backoffMultiplier; }

    public double getJitterRatio() { return jitterRatio; }
    public void setJitterRatio(double jitterRatio) { this.jitterRatio = jitterRatio; }
}