package interview.identity.controller;

import interview.identity.model.EntitlementsResponse;
import interview.identity.service.EntitlementsService;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class EntitlementsController {

    private final EntitlementsService service;

    public EntitlementsController(EntitlementsService service) {
        this.service = service;
    }

    // Example: GET /api/v1/entitlements?region=US
    @GetMapping(value = "/entitlements", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntitlementsResponse entitlements(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "region", required = false, defaultValue = "US") String region
    ) {
        return service.getEntitlements(jwt, region);
    }
}