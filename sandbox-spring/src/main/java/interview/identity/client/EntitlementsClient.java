package interview.identity.client;

import java.util.List;

public interface EntitlementsClient {
    List<String> fetchEntitlements(String userId, String region);
}
