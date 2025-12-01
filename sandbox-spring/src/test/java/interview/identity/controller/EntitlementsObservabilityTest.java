package interview.identity.controller;

import interview.identity.service.EntitlementsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EntitlementsObservabilityTest {

    @Autowired MockMvc mvc;

    @Test
    void correlationIdHeader_isReturned_onResponses() throws Exception {
        mvc.perform(get("/api/v1/entitlements")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_entitlements.read")))
                        .param("region", "US"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void correlationId_isPropagatedWhenProvided() throws Exception {
        mvc.perform(get("/api/v1/entitlements")
                        .header("X-Correlation-Id", "test-cid-123")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_entitlements.read")))
                        .param("region", "US"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "test-cid-123"));
    }

    @Test
    void correlationId_isGeneratedWhenMissing() throws Exception {
        mvc.perform(get("/api/v1/entitlements")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_entitlements.read")))
                        .param("region", "US"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));
    }
}
