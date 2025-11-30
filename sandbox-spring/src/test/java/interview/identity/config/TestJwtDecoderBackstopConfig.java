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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@Profile("test")
public class TestJwtDecoderBackstopConfig {

    @Bean
    @Primary
    public JwtDecoder jwtDecoder(JwtProperties props) {
        // Use first trusted issuer + its secret (legacy/single configs can be normalized in JwtProperties)
        String issuer = props.getIssuers().get(0).getIssuer();
        String secret = props.getIssuers().stream()
                .filter(issuerConfig -> issuerConfig.getIssuer().equals(issuer))
                .map(JwtProperties.IssuerConfig::getHmacSecret)
                .findFirst()
                .orElse(null);

        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(JwtValidationPolicy.build(issuer, props.getAudience(), props.getClockSkew()));
        return decoder;
    }
}
