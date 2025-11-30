package interview.identity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import interview.identity.security.JsonAccessDeniedHandler;
import interview.identity.security.JsonAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class WebSecurityConfig {

    @Bean
    public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(
            JwtProperties props,
            JwtDecoderFactory<String> decoderFactory
    ) {
        Set<String> allowlist = props.getIssuers().stream()
                .map(JwtProperties.IssuerConfig::getIssuer)
                .collect(Collectors.toSet());

        var cache = new ConcurrentHashMap<String, AuthenticationManager>();

        return new JwtIssuerAuthenticationManagerResolver((String issuer) -> {
            if (!allowlist.contains(issuer)) return null; // unknown issuer => 401
            return cache.computeIfAbsent(issuer, iss -> {
                JwtDecoder decoder = decoderFactory.createDecoder(iss);
                JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
                return provider::authenticate;
            });
        });
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper om,
            AuthenticationManagerResolver<HttpServletRequest> resolver
    ) throws Exception {

        var invalidToken401 =
                new JsonAuthenticationEntryPoint(om, "INVALID_TOKEN", "Missing or invalid access token");
        var insufficientScope403 =
                new JsonAccessDeniedHandler(om, "INSUFFICIENT_SCOPE", "Missing required scope: entitlements.read");

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/healthz").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .authenticationManagerResolver(resolver)
                        // ✅ ensure JWT decode failures return JSON (401)
                        .authenticationEntryPoint(invalidToken401)
                        // ✅ ensure missing scope returns JSON (403)
                        .accessDeniedHandler(insufficientScope403)
                )
                // you can keep these as a belt-and-suspenders fallback
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(invalidToken401)
                        .accessDeniedHandler(insufficientScope403)
                )
                .build();
    }
}
