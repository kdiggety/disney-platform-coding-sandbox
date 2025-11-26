# Disney Platform Engineering – Coding Interview Sandboxes (Java + Spring Boot)

This repo contains two **paste-and-run** sandboxes you can use during a live coding interview where the interviewer provides a code sample via chat.

- **`sandbox-java/`**: lightweight Java 17 + Maven + JUnit 5 (+ Mockito) for any plain Java snippet.
- **`sandbox-spring/`**: minimal Spring Boot 3 app (Web + Validation + Actuator + Test) when the sample resembles a microservice.

## Prereqs

- **Java 17+**
- **Maven 3.9+**

## Quick start

### Option A: Plain Java sandbox (recommended default)

```bash
cd sandbox-java
mvn -q test
```

### Option B: Spring Boot sandbox

```bash
cd sandbox-spring
mvn -q test
mvn -q spring-boot:run
```

Then hit: `http://localhost:8080/actuator/health`

## During the interview (copy/paste workflow)

1. Pick the sandbox that matches the sample:
   - Plain classes / utilities / simple services → **`sandbox-java`**
   - Controllers / HTTP / configuration / service patterns → **`sandbox-spring`**
2. Create files under:
   - `src/main/java/interview/...`
   - `src/test/java/interview/...`
3. Adjust `package` lines in pasted code to match `package interview;` (or create subpackages).
4. Keep a tight loop:
   - `mvn -q test` after each meaningful change
5. Prefer **small, safe improvements** with tests (characterization test → refactor → feature).

## Repo layout

```
.
├── sandbox-java
│   ├── pom.xml
│   └── src/...
└── sandbox-spring
    ├── pom.xml
    └── src/...
```

## Tips

- Keep a scratchpad file open (P0/P1 issues, TODOs).
- Consider adding tests first to lock behavior before refactors.
- Use AI/Google as you normally would, but review and explain anything you paste.

---

Good luck and have fun shipping safe improvements.
