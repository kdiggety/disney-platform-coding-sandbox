package interview.identity.service;

import interview.identity.model.EntitlementsResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class EntitlementsService {
    private static final Logger log = LoggerFactory.getLogger(EntitlementsService.class);

    public EntitlementsResponse getEntitlements(Jwt jwt, String region) {
        return new EntitlementsResponse(jwt.getSubject(), region, List.of("PLAN_BASIC", "ESPN_PLUS"));
    }
}
