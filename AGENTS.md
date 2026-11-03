# Repository Guidelines

## Project Structure
- Source: `src/main/java/com/ravzeno/droppers_service/`
- Tests: `src/test/java/com/ravzeno/droppers_service/`
- Resources/config: `src/main/resources/` (Liquibase changelogs in `db/changelogs/`)
- Generated code: `src/main/java/com/ravzeno/droppers_service/` (QueryDSL Q-classes — do not edit)
- Build output: `build/`
- Single-module Gradle project: `build.gradle.kts`, `settings.gradle.kts`

## Build, Test, Run
- `make build` — clean + build JAR.
- `make test` — clean + unit tests (H2 in-memory DB, Liquibase disabled).
- `make run` — run locally (`./gradlew bootRun`).
- `make format` — apply code formatting (`./gradlew spotlessApply`).
- `make format-check` — check formatting without modifying (`./gradlew spotlessCheck`).
- `./gradlew generatePermissions` — generate permissions from backend API.

## CI Pipeline Order
```
build → spotlessCheck → test
```

## Integration Tests
```bash
./gradlew test --tests "*ItTest" --spring.profiles.active=integration
```
Requires PostgreSQL Testcontainers via `src/test/resources/application-integration.yml`.

## Coding Style
- Java 21 (toolchain), 4-space indent.
- Package: `com.x_connext.go_max_service...`
- Classes: `PascalCase`; methods/fields: `camelCase`; constants: `UPPER_SNAKE_CASE`.
- Immutable DTOs preferred; throw domain exceptions from `.../exceptions/core/`.
- Sensitive data handled by converters in `.../commons/model/converter/` (ThaiIdCard, IdentifyNo, PassportNo — both hash and cipher variants).
- Prefer method references over lambdas when a lambda only delegates to one method call (for example, `Type::getProperty`). Keep lambdas when they perform a transformation, condition, or other logic that a method reference would obscure.

## Null-Safe Utilities

### When to Use
- **Chain property access** like `obj.getA().getB().getC()` → use `NullSafeUtils.get()`
- **Instant → LocalDate conversion** → use `DateTimeUtils.toLocalDate()`
- **Nested null checks** → use `NullSafeUtils.get(obj, Class::getProperty)`

### Utilities Location
- `commons/utils/NullSafeUtils.java` — null-safe property access
- `commons/utils/DateTimeUtils.java` — null-safe date/time conversion

### Pattern Examples
```java
// BAD: obj.getA().getB().getC() → NPE if any is null
String name = claim.getHealthClaim().getHospital().getName();

// GOOD: Extract intermediate with NullSafeUtils.get()
var hospital = NullSafeUtils.get(claim.getHealthClaim(), h -> h.getHospital());
String name = NullSafeUtils.get(hospital, Hospital::getName);

// BEST: Step-by-step with explicit variables
var healthClaim = NullSafeUtils.get(claim, PolicyClaim::getHealthClaim);
var hospital = NullSafeUtils.get(healthClaim, PolicyHealthClaim::getHospital);
String name = NullSafeUtils.get(hospital, Hospital::getName);
```

## Testing
- JUnit 5 with Spring Boot Test. Test classes end with `Test` or `*ItTest`.
- Unit tests: default `application.yml` (H2, no Liquibase).
- Integration tests (`*ItTest`): `application-integration.yml` + Testcontainers PostgreSQL.
- Test profiles resolved via `spring.profiles.active`. Custom resolvers in `ci/` packages.
- Reports: `build/reports/tests/test/index.html`.
- Reusable unit-test structure, Mockito injection, nested test grouping, and Arrange/Act/Assert guidance: [`TESTING.md`](TESTING.md).

## Architecture
- `domains/` — business logic (order, policy, product, insurance, country, etc.)
- `external_service/` — HTTP clients for OTP, payment, message, file, discount, One-ID, i-motor, one-healthcare, SFTP.
- `library/` — payment, message, i-motor clients.
- `scheduler/` — scheduled jobs and workers (Thai Health report generation, etc.)
- `commons/` — shared utilities, filters, exceptions, converters, Excel helpers.

## External Services & Config
All external services configured via `application.yml` env vars:
- OTP, payment, message, file, discount, One-ID, turnstile, i-motor, one-healthcare
- SFTP (Thai Health integration server)
- Redis, PostgreSQL, S3 (AWS)
- Secrets never hardcoded — use env var placeholders.

## Database
- Liquibase for migrations (`db/changelog-master.xml`).
- Sensitive PII columns use custom converters (hash/cipher) — see `.../converter/`.
- `REQUIRES_NEW` transaction propagation used in some scheduled task services.

## Security
- JWT-based auth; blacklisting supported.
- OCR feature flag opt-in (`features.ocr`).
- Error responses use typed exceptions (`BadRequestException`, `UnauthorizedException`, `ForbiddenException`, `ResourceNotFoundException`, `ResourceConflictException`, `BusinessLogicException`, `InternalServerException`, `TooManyRequestException`).

## Domain Implementation Pattern
- Keep controllers focused on HTTP binding, validation, and delegating to services. Preserve each endpoint's existing response contract.
- For new or modified API endpoints, controller methods must return responses using the project's `SuccessResponse` wrapper. Keep the response data inside `SuccessResponse` and preserve the established endpoint contract.
- Put business rules and transaction boundaries in services. Mark read-only database operations with `@Transactional(readOnly = true)` and writes with `@Transactional` when the operation spans repository calls.
- Keep JPA entities under a domain's `model/` package and persistence access in Spring Data repository interfaces. Keep entities free of request/response DTO conversion.
- Put DTOs under a domain's `dto/` package and entity-to-DTO conversion in a domain `*Mapper` service. Services return DTOs to controllers; controllers should not map entities.
- Use a dedicated `query/` class and repository facade for custom QueryDSL list/count queries. Keep ordinary derived or annotated JPA queries in the domain repository.
- For domains backed by the filesystem or another non-JPA store, keep the same controller → service → mapper separation without adding artificial repositories or entities.
