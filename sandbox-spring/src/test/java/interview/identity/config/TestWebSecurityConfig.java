package interview.identity.config;

import interview.identity.security.JwtValidationPolicy;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("test")
public class TestWebSecurityConfig {

    @Bean(name = "testJwtDecoder")
    public JwtDecoder jwtDecoder(
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.audience}") String audience,
            @Value("${app.jwt.clock-skew-seconds:60}") long skewSeconds,
            @Value("${app.jwt.hmac-secret}") String secret
    ) {
        // Local-only decoder for tests; no calls to issuer-uri / JWKS.
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(JwtValidationPolicy.build(issuer, audience, Duration.ofSeconds(skewSeconds)));
        return decoder;
    }
}
