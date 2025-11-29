package interview.identity.config;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "app.jwt.issuer=https://issuer.example.com/",
        "app.jwt.audience=entitlements-api",
        "app.jwt.hmac-secret=0123456789abcdef0123456789abcdef"
})
class JwtDecoderVerificationTest {

    @Autowired JwtDecoder jwtDecoder;

    private static final String ISSUER = "https://issuer.example.com/";
    private static final String AUD = "entitlements-api";

    // --- #3: Header/algorithm supported ---

    @Test
    void rejects_alg_none() {
        String token = algNoneToken("user-123", ISSUER, AUD);
        assertThrows(JwtException.class, () -> jwtDecoder.decode(token));
    }

    @Test
    void rejects_unsupported_algorithm_hs384_when_decoder_requires_hs256() throws Exception {
        // HS384 token (validly signed) should be rejected because decoder is configured for HS256 only.
        String longSecret = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"; // 64 chars
        String token = signedHsToken(JWSAlgorithm.HS384, longSecret, "user-123", ISSUER, AUD, Instant.now().plusSeconds(300));
        assertThrows(JwtException.class, () -> jwtDecoder.decode(token));
    }

    // --- #4: Payload claim validation (iss, aud, exp) ---

    @Test
    void rejects_wrong_issuer() throws Exception {
        String token = signedHsToken(JWSAlgorithm.HS256,
                "0123456789abcdef0123456789abcdef",
                "user-123",
                "https://evil.example.com/",      // wrong iss
                AUD,
                Instant.now().plusSeconds(300));

        assertThrows(JwtException.class, () -> jwtDecoder.decode(token));
    }

    @Test
    void rejects_wrong_audience() throws Exception {
        String token = signedHsToken(JWSAlgorithm.HS256,
                "0123456789abcdef0123456789abcdef",
                "user-123",
                ISSUER,
                "wrong-audience",
                Instant.now().plusSeconds(300));

        assertThrows(JwtException.class, () -> jwtDecoder.decode(token));
    }

    @Test
    void rejects_expired_token_beyond_clock_skew() throws Exception {
        // config allows 60s skew; expire it far enough in the past to still fail
        String token = signedHsToken(JWSAlgorithm.HS256,
                "0123456789abcdef0123456789abcdef",
                "user-123",
                ISSUER,
                AUD,
                Instant.now().minusSeconds(600));

        assertThrows(JwtException.class, () -> jwtDecoder.decode(token));
    }

    // --- #5: Signature validation ---

    @Test
    void rejects_bad_signature_signed_with_different_secret() throws Exception {
        String token = signedHsToken(JWSAlgorithm.HS256,
                "BADBADBADBADBADBADBADBADBADBADBA", // different 32-char secret
                "user-123",
                ISSUER,
                AUD,
                Instant.now().plusSeconds(300));

        assertThrows(JwtException.class, () -> jwtDecoder.decode(token));
    }

    // ---------------- helpers ----------------

    private static String signedHsToken(
            JWSAlgorithm alg,
            String secret,
            String sub,
            String issuer,
            String audience,
            Instant exp
    ) throws Exception {

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(sub)
                .issuer(issuer)
                .audience(List.of(audience))
                .issueTime(new Date())
                .expirationTime(Date.from(exp))
                .build();

        JWSHeader header = new JWSHeader.Builder(alg)
                .type(JOSEObjectType.JWT)
                .keyID("test-kid-1")
                .build();

        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }

    private static String algNoneToken(String sub, String issuer, String audience) {
        String headerJson = "{\"alg\":\"none\",\"typ\":\"JWT\"}";
        String payloadJson = "{\"sub\":\"" + sub + "\",\"iss\":\"" + issuer + "\",\"aud\":\"" + audience + "\"}";

        String h = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        String p = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

        // 3 segments, but no real signature
        return h + "." + p + ".";
    }
}
