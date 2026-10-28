# Droppers Service

Droppers Service is the Spring Boot backend for a local-first file management application. It provides APIs for browsing and managing files, upload transfers, trash, trusted devices, and device-pairing flows. Metadata is stored locally in SQLite.

## Requirements

- Java 21
- A writable directory for the SQLite database and configured storage root

The Gradle wrapper is included, so a separate Gradle installation is not required.

## Run locally

Start the application with the development script:

```bash
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
| `DROPPERS_DB` | `~/.local/share/droppers/droppers.db` | SQLite database location. |
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
src/main/java/       Application source and HTTP API
src/main/resources/  Spring configuration and SQLite schema
src/test/java/       Unit and integration-style tests
scripts/             Local development helpers
```
