package interview.identity.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import interview.identity.config.JwtProperties;
import interview.identity.model.EntitlementsResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EntitlementsMultiIssuerSecurityTest {

    private static final String ISSUER_A = "https://issuer-a.example.com";
    private static final String ISSUER_B = "https://issuer-b.example.com";
    private static final String UNKNOWN_ISSUER = "https://evil.example.com";

    @Autowired MockMvc mvc;
    @Autowired JwtProperties props;

    @MockBean EntitlementsService service;

    private String mintHs256(String issuer, String subject, String scope, String signingSecret) {
        SecretKey key = new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(subject)
                .audience(List.of(props.getAudience()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claim("scope", scope) // -> SCOPE_entitlements.read
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Test
    void issuerA_accepted() throws Exception {
        given(service.getEntitlements(any(), eq("US")))
                .willReturn(new EntitlementsResponse("user-123", "US", List.of("PLAN_BASIC")));

        String token = mintHs256(
                ISSUER_A,
                "user-123",
                "entitlements.read",
                Objects.requireNonNull(props.getIssuers().stream()
                        .filter(issuerConfig -> ISSUER_A.equals(issuerConfig.getIssuer()))
                        .map(JwtProperties.IssuerConfig::getHmacSecret)
                        .findFirst()
                        .orElse(null))
        );

        mvc.perform(get("/api/v1/entitlements")
                        .param("region", "US")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user-123"));
    }

    @Test
    void issuerB_rejected() throws Exception {
        // Token *claims* issuer B, but is signed with issuer A secret => should fail signature when routed to issuer B decoder
        String token = mintHs256(
                ISSUER_B,
                "user-123",
                "entitlements.read",
                // WRONG key on purpose
                Objects.requireNonNull(props.getIssuers().stream()
                        .filter(issuerConfig -> ISSUER_A.equals(issuerConfig.getIssuer()))
                        .map(JwtProperties.IssuerConfig::getHmacSecret)
                        .findFirst()
                        .orElse(null))
        );

        mvc.perform(get("/api/v1/entitlements")
                        .param("region", "US")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void unknown_issuer_rejected() throws Exception {
        String token = mintHs256(
                UNKNOWN_ISSUER,
                "user-123",
                "entitlements.read",
                // any secret; should be rejected before decode because issuer not allowlisted
                Objects.requireNonNull(props.getIssuers().stream()
                        .filter(issuerConfig -> ISSUER_A.equals(issuerConfig.getIssuer()))
                        .map(JwtProperties.IssuerConfig::getHmacSecret)
                        .findFirst()
                        .orElse(null))
        );

        mvc.perform(get("/api/v1/entitlements")
                        .param("region", "US")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }
}
