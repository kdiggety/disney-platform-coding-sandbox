package interview.identity.controller;

import interview.identity.model.EntitlementsResponse;
import interview.identity.service.EntitlementsService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class EntitlementsController {

    private final EntitlementsService service;

    public EntitlementsController(EntitlementsService service) {
        this.service = service;
    }

    // Example: GET /api/v1/entitlements?token=xxx&region=US
    @GetMapping(value = "/entitlements", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntitlementsResponse entitlements(
            @RequestParam("token") String token,
            @RequestParam(value = "region", required = false, defaultValue = "US") String region
    ) {
        return service.getEntitlements(token, region);
    }
}