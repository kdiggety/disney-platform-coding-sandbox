package interview.identity.config;

import interview.identity.security.JwtValidationPolicy;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@Profile("test")
public class TestJwtDecoderFactoryConfig {

    @Bean
    @Primary
    public JwtDecoderFactory<String> issuerJwtDecoderFactory(JwtProperties props) {
        return issuer -> {
            String secret =
                    props.getIssuers().stream()
                            .filter(issuerConfig -> issuerConfig.getIssuer().equals(issuer))
                            .map(JwtProperties.IssuerConfig::getHmacSecret)
                            .findFirst()
                            .orElse(null);
            if (secret == null) {
                throw new IllegalArgumentException("No test HMAC secret configured for issuer: " + issuer);
            }
            SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();

            decoder.setJwtValidator(JwtValidationPolicy.build(
                    issuer, props.getAudience(), props.getClockSkew()
            ));
            return decoder;
        };
    }
}
