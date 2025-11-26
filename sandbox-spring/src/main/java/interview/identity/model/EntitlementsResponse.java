package interview.identity.model;

import java.util.List;

public record EntitlementsResponse(
        String userId,
        String region,
        List<String> entitlements
) {}
