# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**chainvault** is a Spring Boot 4 + Java 25 document orchestration service. It processes incoming ZIP archives through a Flowable BPMN 2.0 pipeline: extract → hash → OCR (Tesseract) → transform → merge PDF → sign → SFTP upload. A React 19 dashboard shows live migration status via Server-Sent Events.

## Common Commands

All tasks are managed via `mise`. Run these from the project root:

```bash
# Build
mise build             # mvn clean install -DskipTests
mise package           # mvn clean package -DskipTests (JAR only)

# Test
mise test              # mvn integration-test (unit + integration tests)
mise test-docker       # Docker-based Testcontainers tests only
mise verify            # mvn clean verify -Pcoverage (full build + JaCoCo coverage)

# Run a single test class
mvn -pl chainvault-orchestration test -Dtest=MigrationControllerTest
mvn -pl chainvault-orchestration failsafe:integration-test -Dit.test=DockerServicesIT

# Code quality
mise check             # Spotless + ESLint + Prettier checks (no writes)
mise format            # Spotless + ESLint + Prettier auto-format

# Local development
mise dev               # Starts postgres + Spring Boot app on localhost:8085
mise compose-up        # Full stack: app + postgres + SFTP + observability
mise compose-down      # Stop services

# Docker
mise docker-build      # Build image: gryphus-lab/chainvault:latest
```

JaCoCo HTML report after `mise verify`: `chainvault-report-aggregate/target/site/jacoco-aggregate/index.html`

## Module Structure

Maven multi-module project with 4 modules:

|            Module             |                                 Purpose                                  |
|-------------------------------|--------------------------------------------------------------------------|
| `chainvault-migration`        | Business logic: OCR, PDF, SFTP, hashing. No web dependencies.            |
| `chainvault-orchestration`    | Spring Boot app: Flowable engine, REST API, SSE, JPA entities, Liquibase |
| `chainvault-dashboard`        | React 19 + Vite live dashboard (TailwindCSS, TanStack Query, MUI)        |
| `chainvault-admin-ui`         | Legacy CoreUI admin template (not integrated with live data)             |
| `chainvault-report-aggregate` | JaCoCo coverage aggregation only                                         |

The frontend (`chainvault-dashboard`) is built by Maven and bundled into the Spring Boot JAR as static resources. The `SpaController` serves `index.html` for all non-API routes.

## Architecture

### BPMN Workflow

The Flowable process (`chainvault-orchestration/src/main/resources/processes/chainvault.bpmn`) runs these delegates sequentially:

```
AsyncInitVariables → ExtractAndHash → TransformMetadata → PrepareFiles →
PerformOcr → MergePdf → SignDocument → SftpUpload → [End]
                                                          ↓
                                                    HandleError → [End Failed]
```

Each delegate extends `AbstractTracingDelegate` (OpenTelemetry context propagation). Error boundary events on each step route to `HandleErrorDelegate`.

### REST API

- `GET /api/migrations` — list with `?limit=N`
- `GET /api/migrations/stats` — aggregated counts
- `GET /api/migrations/{id}/detail` — full timeline + OCR preview + download URLs
- `GET /api/migrations/events` — SSE stream for live updates

### Database

PostgreSQL 18 with Liquibase migrations in `chainvault-orchestration/src/main/resources/db/changelog/`. Two core tables: `migration_audit` (one row per migration) and `migration_event` (timeline events per step). The `migration_summary_view` is used for stats queries.

### SSE / Live Dashboard

`SseEmitterService` pushes events to connected dashboard clients. The dashboard's `useMigrationEvents` hook (`chainvault-dashboard/src/hooks/useMigrationEvents.ts`) subscribes to `/api/migrations/events` with 3s auto-reconnect backoff.

## Key Configuration

**`application-local.yml`** — active when `SPRING_PROFILES_ACTIVE=local` (set by mise):
- OpenTelemetry disabled
- SFTP: `testuser:testpass123` @ `localhost:2222`
- Source API: `http://localhost:9091`

**Environment variables** set by `mise.toml`:
- `TESSDATA_PREFIX=/opt/homebrew/share/tessdata` (macOS; adjust for your system)
- `SPRING_PROFILES_ACTIVE=local`
- `_JAVA_OPTIONS="-Xmx2048m -XX:+UseZGC -Djdk.virtualThreadScheduler.parallelism=auto"`

## Testing Conventions

- Unit tests: `*Test.java` (Surefire)
- Integration tests: `*IT.java` (Failsafe, may require Docker)
- Docker integration tests live in `src/test/java/docker/` and require a running Docker daemon
- ArchUnit architecture tests live in `src/test/java/arch/`
- Frontend tests use Vitest: `yarn workspace chainvault-dashboard test`

## Code Formatting

- Java: Google Java Format via Spotless (`mise format` or `mvn spotless:apply`)
- Spotless uses ratchet from `origin/main` — only changed files are checked in CI
- TypeScript/JS: Prettier + ESLint (configs in each frontend module)

## Observability

When running with `mise compose-up` (LGTM stack):
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090
- App metrics: http://localhost:8085/actuator/prometheus

OpenTelemetry traces export to OTLP/gRPC at `localhost:4317`. Disabled in the `local` Spring profile.
