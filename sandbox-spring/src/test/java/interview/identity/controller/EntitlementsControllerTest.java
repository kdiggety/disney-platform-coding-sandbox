package interview.identity.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EntitlementsControllerTest {

    @Autowired MockMvc mvc;

    @MockBean JwtDecoder jwtDecoder;

    @BeforeEach
    void setup() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now().minusSeconds(5),
                Instant.now().plusSeconds(300),
                Map.of("alg", "RS256", "typ", "JWT"),
                Map.of(
                        "sub", "user-123",
                        "iss", "https://issuer.example.com/",
                        "aud", List.of("entitlements-api"),
                        "scope", "entitlements.read"
                )
        );
        given(jwtDecoder.decode(anyString())).willReturn(jwt);
    }

    @Test
    void happyPath_returnsEntitlements() throws Exception {
        mvc.perform(get("/api/v1/entitlements")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .param("region", "US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.entitlements[0]").value("PLAN_BASIC"));
    }
}