package interview.identity.controller;

import interview.identity.model.EntitlementsResponse;
import interview.identity.service.EntitlementsService;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EntitlementsControllerTest {

    @Autowired MockMvc mvc;

    @MockBean EntitlementsService service;

    @Test
    void happyPath_returnsEntitlements() throws Exception {
        given(service.getEntitlements(any(Jwt.class), eq("US")))
                .willReturn(new EntitlementsResponse("user-123", "US", List.of("PLAN_BASIC")));

        mvc.perform(get("/api/v1/entitlements")
                        .with(jwt().jwt(j -> j
                                .subject("user-123")
                                .claim("scope", "entitlements.read")
                        ).authorities(new SimpleGrantedAuthority("SCOPE_entitlements.read")))
                        .param("region", "US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.entitlements[0]").value("PLAN_BASIC"));
    }
}
