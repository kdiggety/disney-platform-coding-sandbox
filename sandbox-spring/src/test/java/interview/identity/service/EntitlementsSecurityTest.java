package interview.identity.service;

import interview.identity.model.EntitlementsResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EntitlementsSecurityTest {

    @Autowired MockMvc mvc;

    // Keep the test about security behavior, not downstream logic.
    @MockBean EntitlementsService service;

    @Test
    void returns401_whenMissingToken() throws Exception {
        mvc.perform(get("/api/v1/entitlements").param("region", "US"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void returns403_whenTokenValidButMissingScope() throws Exception {
        mvc.perform(get("/api/v1/entitlements")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_other.scope")))
                        .param("region", "US"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_SCOPE"));
    }

    @Test
    void returns200_whenScopePresent() throws Exception {
        given(service.getEntitlements(any(Jwt.class), eq("US")))
                .willReturn(new EntitlementsResponse("user-123", "US", List.of("PLAN_BASIC")));

        mvc.perform(get("/api/v1/entitlements")
                        .with(jwt().jwt(j -> j.subject("user-123"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_entitlements.read")))
                        .param("region", "US"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user-123"));

        verify(service, times(1)).getEntitlements(any(Jwt.class), eq("US"));
    }
}
