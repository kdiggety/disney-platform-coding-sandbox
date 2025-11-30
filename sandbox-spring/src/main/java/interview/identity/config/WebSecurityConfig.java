package interview.identity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import interview.identity.security.JsonAccessDeniedHandler;
import interview.identity.security.JsonAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper om,
            HandlerMappingIntrospector introspector
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
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {}))
                .exceptionHandling(ex -> ex
                        // ✅ single consistent JSON handler for *all* 401s
                        .authenticationEntryPoint(invalidToken401)
                        // ✅ scoped 403 handler for missing scope
                        .accessDeniedHandler(insufficientScope403)
                )
                .build();
    }
}