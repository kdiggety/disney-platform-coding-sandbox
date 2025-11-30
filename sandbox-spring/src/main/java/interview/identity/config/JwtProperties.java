package interview.identity.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String audience = "entitlements-api";
    private Duration clockSkew = Duration.ofSeconds(60);
    private List<IssuerConfig> issuers = new ArrayList<>();

    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }

    public Duration getClockSkew() { return clockSkew; }
    public void setClockSkew(Duration clockSkew) { this.clockSkew = clockSkew; }

    public List<IssuerConfig> getIssuers() { return issuers; }
    public void setIssuers(List<IssuerConfig> issuers) { this.issuers = issuers; }

    public static class IssuerConfig {
        private String issuer;
        private String hmacSecret; // test only; prod would use JWKS
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getHmacSecret() { return hmacSecret; }
        public void setHmacSecret(String hmacSecret) { this.hmacSecret = hmacSecret; }
    }
}
