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

### Option A (preferred): Internal Spring Boot Starter / Module

**Goal:** Package multi-issuer JWT validation + consistent 401/403 handling + scope enforcement patterns into a reusable dependency teams can adopt with minimal code.

#### Deliverables
- [ ] Create repo/module structure:
    - [ ] `security-autoconfigure` (JAR): contains auto-config + properties + handlers
    - [ ] `security-starter` (POM/JAR): depends on autoconfigure and pulls required deps (resource server, jose, etc.)
- [ ] Move common security components into `security-autoconfigure`:
    - [ ] `JwtProperties` (`app.jwt.*`): `issuers[]`, `audience`, `clockSkewSeconds`, optional test-only `hmacSecretsByIssuer`
    - [ ] `JwtValidationPolicy`: issuer + audience + timestamp validation
    - [ ] `JsonAuthenticationEntryPoint` (401) + `JsonAccessDeniedHandler` (403) producing stable error codes
    - [ ] Multi-issuer `AuthenticationManagerResolver<HttpServletRequest>` with allowlist + per-issuer decoder caching
    - [ ] Default `SecurityFilterChain` with:
        - `.oauth2ResourceServer(o -> o.authenticationManagerResolver(resolver))`
        - `.exceptionHandling(ex -> ex.authenticationEntryPoint(json401).accessDeniedHandler(json403))`
        - `.authorizeHttpRequests(...)` baseline authenticated-by-default
    - [ ] Ensure overrideability:
        - [ ] `@ConditionalOnMissingBean(SecurityFilterChain.class)`
        - [ ] `@ConditionalOnMissingBean(AuthenticationManagerResolver.class)`
- [ ] Register auto-configuration (Boot 3):
    - [ ] `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
        - `com.yourco.security.autoconfigure.SecurityAutoConfiguration`
- [ ] Provide a reusable scope enforcement pattern (pick one):
    - [ ] Pattern 1 (constants): `Scopes.ENTITLEMENTS_READ = "SCOPE_entitlements.read"` for `@PreAuthorize`
    - [ ] Pattern 2 (helper bean): `@Component ScopeChecker` used as `@PreAuthorize("@scope.has(authentication,'entitlements.read')")`
- [ ] Provide “test mode” support for local dev and unit tests:
    - [ ] In tests, use deterministic HS256 secrets per issuer (no network)
    - [ ] Provide a `JwtTestSupport` utility to mint HS256 JWTs with `iss/aud/scope` for integration tests
- [ ] Publish usage instructions:
    - [ ] Example dependency snippet
    - [ ] Example `application.yaml`:
        - `app.jwt.audience`
        - `app.jwt.clock-skew-seconds`
        - `app.jwt.issuers[].issuer`
        - (optional test) `app.jwt.issuers[].hmac-secret`

#### Adoption flow for teams
- [ ] Add dependency: `com.yourco.security:security-starter`
- [ ] Set `app.jwt.*` properties
- [ ] Add `@PreAuthorize(...)` scope checks (or shared annotations/constants)
- [ ] Get consistent 401/403 JSON errors + multi-issuer validation by default

---

### Option B (platform evolution): Baseline auth in Istio + app-level authz

**Goal:** Centralize JWT verification and coarse policy in Istio; keep business/endpoint authorization in the app.

#### Istio responsibilities (gateway + sidecar)
- [ ] JWT verification (RequestAuthentication):
    - [ ] signature/JWKS resolution
    - [ ] issuer allowlist
    - [ ] token lifetime checks (exp/nbf)
- [ ] Coarse authorization (AuthorizationPolicy):
    - [ ] require authenticated principal
    - [ ] optionally require broad permission/role at perimeter
    - [ ] prefer array-style claims (`permissions: [...]`, `roles: [...]`) for reliable membership checks

#### App responsibilities
- [ ] Fine-grained authorization:
    - [ ] per-endpoint `scope` checks via Spring Security (`@PreAuthorize`)
    - [ ] tenant/region/business rules
- [ ] Maintain consistent API error contract (401/403 JSON) and domain-specific decisions

#### Key note / tradeoff
- [ ] Istio policy matching is most robust with array claims (e.g., `permissions: ["entitlements.read"]`).
    - If IdP only emits space-delimited `scope` strings, prefer keeping scope enforcement in-app while Istio does baseline auth.
