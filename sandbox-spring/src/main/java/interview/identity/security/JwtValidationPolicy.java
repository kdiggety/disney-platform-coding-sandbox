package interview.identity.security;

import java.time.Duration;
import java.util.List;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

public final class JwtValidationPolicy {
    private JwtValidationPolicy() {}

    public static OAuth2TokenValidator<Jwt> build(
            String issuer,
            String audience,
            Duration clockSkew
    ) {
        OAuth2TokenValidator<Jwt> issuerValidator = new JwtIssuerValidator(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            List<String> aud = jwt.getAudience();
            return (aud != null && aud.contains(audience))
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Missing/invalid audience", null)
            );
        };

        JwtTimestampValidator tsValidator = new JwtTimestampValidator(clockSkew);

        return new DelegatingOAuth2TokenValidator<>(issuerValidator, tsValidator, audienceValidator);
    }
}
