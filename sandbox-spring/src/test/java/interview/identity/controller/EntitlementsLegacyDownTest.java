package interview.identity.controller;

import interview.identity.exception.LegacyEntitlementsUnavailableException;
import interview.identity.service.EntitlementsService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EntitlementsLegacyDownTest {

    @Autowired MockMvc mvc;

    @MockBean EntitlementsService service;

    @Test
    void legacyDown_returns503_withStableErrorCode() throws Exception {
        given(service.getEntitlements(any(), eq("US")))
                .willThrow(new LegacyEntitlementsUnavailableException("down"));

        mvc.perform(get("/api/v1/entitlements")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_entitlements.read")))
                        .param("region", "US"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("LEGACY_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Legacy entitlements service is unavailable"));
    }
}
