package interview.identity.config;

import interview.identity.security.JwtValidationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@Profile("!test")
public class JwtDecoderFactoryConfig {

    @Bean
    public JwtDecoderFactory<String> issuerJwtDecoderFactory(JwtProperties props) {
        return issuer -> {
            JwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
            if (decoder instanceof NimbusJwtDecoder nimbus) {
                nimbus.setJwtValidator(JwtValidationPolicy.build(
                        issuer, props.getAudience(), props.getClockSkew()
                ));
                return nimbus;
            }
            throw new IllegalStateException("Expected NimbusJwtDecoder for issuer " + issuer);
        };
    }
}
