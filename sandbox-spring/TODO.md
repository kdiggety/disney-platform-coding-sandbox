# Open Issues:

## High
 - [x] Receiving token via request parameter instead of HTTP header - interview/identity/controller/EntitlementsController.java:21
 - [x] Logging the token - interview/identity/service/EntitlementsService.java:16
 - [x] Does not validate the header, primarily if the algorithm is supported - interview.identity.util.JwtUtil.extractSubWithoutVerification
 - [x] Does not validate the payload, if the issuer or audience are supported, also does not validate the token expiry - interview.identity.util.JwtUtil.extractSubWithoutVerification
 - [x] Does not validate the signature - interview.identity.util.JwtUtil.extractSubWithoutVerification

## Medium
- [x] HttpClient is missing connection timeout - interview/identity/client/LegacyEntitlementsClient.java:22
- [x] Hard-coded configuration - interview/identity/client/LegacyEntitlementsClient.java:26-28
- [x] Hard-coded retry loop, missing exponential backoff - interview/identity/client/LegacyEntitlementsClient.java:35
- [x] No test coverage - interview/identity/client/LegacyEntitlementsClient.java:31-62
- [x] Missing request input validation - interview/identity/controller/EntitlementsController.java:19-25
- [x] Revocation store is not thread-safe - interview/identity/service/TokenRevocationStore.java:11
- [x] Revocation store is missing TTL - interview/identity/service/TokenRevocationStore.java:11

Low
- [ ] Missing class and method level comments - interview.identity.client.EntitlementsClient
- [ ] Fragile CSV parsing - interview/identity/client/LegacyEntitlementsClient.java:65-70
- [ ] Missing Javadocs - ALL CLASSES

# Open Tasks:

## Challenge 2 — Authorization / scopes (feature add)
- [x] Require entitlements.read scope for /api/v1/entitlements 
- [x] Return 403 with {code:"INSUFFICIENT_SCOPE"} when missing 
- [x] Add tests for missing/incorrect scope

## Challenge 3 — Multi-issuer support (platform design)
- [x] Support multiple issuers safely (allowlist)
- [x] Route to issuer-specific JwtDecoder (prod: JWKS per issuer; test: deterministic local)
- [x] Add tests: issuer A accepted, issuer B rejected, unknown issuer rejected

## Challenge 4 — Operational hardening (platform excellence)
- [x] Observability: metrics + structured logs + correlation id 
- [x] Resilience: circuit breaker / bulkhead + retry policy for legacy client (you basically did retries; circuit breaker is next)
- [x] Clear error mapping: legacy down → 503 with stable error code

## Challenge 5 — “Make it reusable across teams”
- [ ] Extract the JWT validation + scope enforcement into a small internal starter/module 
- [ ] Or show how you’d shift baseline auth to Istio policies while still doing app-level authz