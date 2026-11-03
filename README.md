# Droppers Service

Droppers Service is the Spring Boot backend for a local-first file management application. It provides APIs for browsing and managing files, upload transfers, trash, trusted devices, and device-pairing flows. File contents remain on the configured filesystem; application metadata is stored in PostgreSQL.

## Requirements

- Java 21
- PostgreSQL
- A configured storage root with read and write access

The Gradle wrapper is included, so a separate Gradle installation is not required.

## Run locally

Start the application with the development script:

```bash
psql -U postgres -c "CREATE ROLE droppers LOGIN PASSWORD 'droppers';"
createdb -U postgres -O droppers droppers
bash scripts/dev-server.sh
```

Or use Gradle directly:

```bash
./gradlew bootRun
```

By default, the server listens at `http://127.0.0.1:8765`. Confirm that it is running:

```bash
curl http://127.0.0.1:8765/api/v1/health
```

## Configuration

Configuration is supplied through environment variables. Defaults are designed for local use.

| Variable | Default | Purpose |
| --- | --- | --- |
| `DROPPERS_BIND_ADDRESS` | `127.0.0.1` | Network address the server binds to. |
| `DATABASE_HOST` | `localhost` | PostgreSQL server host. |
| `DATABASE_PORT` | `5432` | PostgreSQL server port. |
| `DATABASE_NAME` | `droppers` | PostgreSQL database name. Create the database before starting the app. |
| `DATABASE_USER` | `droppers` | PostgreSQL login role. |
| `DATABASE_PASSWORD` | `droppers` | PostgreSQL password. Override the local development default outside a local machine. |
| `DROPPERS_ALLOWED_HOSTS` | `localhost,127.0.0.1,::1` | Allowed request host values. |
| `DROPPERS_RATE_LIMIT_PER_MINUTE` | `120` | Per-client request limit. |
| `DROPPERS_REQUIRE_TLS_FOR_LAN` | `true` | Requires TLS when serving LAN clients. |
| `DROPPERS_TLS_ENABLED` | `false` | Enables Spring Boot TLS. |
| `DROPPERS_TLS_KEY_STORE` | — | TLS keystore path. |
| `DROPPERS_TLS_KEY_STORE_PASSWORD` | — | TLS keystore password. |
| `DROPPERS_TLS_KEY_ALIAS` | — | TLS key alias. |
| `DROPPERS_DEVICE_SESSION_TTL_DAYS` | `30` | Lifetime of a device session. |

For example, to bind to a LAN interface, configure TLS and explicitly set the address:

```bash
export DROPPERS_BIND_ADDRESS=0.0.0.0
export DROPPERS_TLS_ENABLED=true
export DROPPERS_TLS_KEY_STORE=/path/to/keystore.p12
bash scripts/dev-server.sh
```

## API overview

All endpoints are prefixed with `/api/v1`.

| Area | Endpoints |
| --- | --- |
| Health and storage | `GET /health`, `GET /storage`, `GET /storage/root`, `PUT /storage/root` |
| Files and folders | `GET /files`, `POST /folders`, `PATCH /files/rename`, `POST /files/move`, `POST /files/upload`, `GET /files/download`, `DELETE /files` |
| Upload transfers | `POST /transfers/uploads`, `PUT /transfers/uploads/{id}/content`, `GET /transfers/uploads`, `GET /transfers/uploads/{id}`, `DELETE /transfers/uploads/{id}` |
| Trash | `GET /trash`, `POST /trash/{id}/restore`, `DELETE /trash/{id}` |
| Devices | `GET /devices`, `DELETE /devices/{id}` |
| Pairing | `POST /pairing`, `POST /pairing/claim`, plus pairing-claim management under `/pairing/claims` |
| Mobile storage | `GET /mobile/storage` |

Paths sent to file APIs are relative to the configured storage root. Set that root before manipulating files:

```bash
curl -X PUT http://127.0.0.1:8765/api/v1/storage/root \
  -H 'Content-Type: application/json' \
  -d '{"path":"/home/you/Droppers"}'
```

## Development

Run the test suite:

```bash
./gradlew test
```

Format source files and verify formatting:

```bash
./gradlew spotlessApply
./gradlew spotlessCheck
```

## Project layout

```
src/main/java/com/ravzeno/droppers_service/
  commons/             Shared configuration, converters, security, and exceptions
  domains/<feature>/   Feature-focused code (device, file, pairing, storage, ...)
    *Repository.java   Spring Data repositories
    query/             Custom QueryDSL queries and repository facades
    dto/              Request and response types
    model/            Domain models, enums, and JPA entities
src/main/resources/   Spring configuration and Liquibase database changelogs
src/test/java/        Unit and integration-style tests
scripts/              Local development helpers
```

Keep feature entities and enums in `model`, and use Spring Data repositories
directly from services/components. Avoid wrapper repositories that only forward
calls; keep multi-entity transaction boundaries in the service. Entities should
map the PostgreSQL schema, store enums as strings, and use the shared converter
for `Instant` columns.

Database schema changes are versioned in `src/main/resources/db/changelogs/`
and applied by Liquibase at application startup. The initial PostgreSQL schema
is created automatically in the configured database. Existing SQLite data is
not copied automatically; export and migrate it separately before switching an
existing installation to PostgreSQL.
