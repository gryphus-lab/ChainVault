# chainvault

**Lightweight orchestration & migration engine for secure document processing**  
Used by Gryphus Lab to coordinate extraction, transformation, signing, merging and secure SFTP delivery of documents.

[![Java 25](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4.0+-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![React](https://img.shields.io/badge/React-19+-2496ED?logo=react&logoColor=white)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-336791?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-managed-2962FF)](https://www.liquibase.org/)
[![Flowable](https://img.shields.io/badge/orchestrated%20with-Flowable-0072C6)](https://www.flowable.com/)
[![mise](https://img.shields.io/badge/managed%20with-mise-6f42c1)](https://mise.jdx.dev/)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=gryphus-lab_chainvault&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=gryphus-lab_chainvault)
[![GitHub Actions CI](https://github.com/gryphus-lab/chainvault/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/gryphus-lab/chainvault/actions/workflows/ci.yml)
[![Qodana](https://github.com/gryphus-lab/chainvault/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/gryphus-lab/chainvault/actions/workflows/qodana_code_quality.yml)

## At a Glance

|      Aspect       |              Technology              |
|-------------------|--------------------------------------|
| Language          | Java 25, Typescript 5.9.3            |
| Framework         | Spring Boot 4, React 19 + Vite       |
| Orchestration     | Flowable (BPMN 2.0)                  |
| Database          | PostgreSQL 18 (Docker)               |
| Schema Migrations | Liquibase (YAML)                     |
| Developer Tooling | mise                                 |
| Observability     | Prometheus + Loki + Grafana + Alloy  |
| CI / Quality      | GitHub Actions + SonarCloud + Qodana |
| Testing           | JUnit 5 + Testcontainers             |
| API Documentation | springdoc OpenAPI / Swagger UI       |

## Overview

Chainvault is a **Spring Boot 4 + Java 25** orchestration service that executes BPMN workflows to:

- extract & hash documents
- transform metadata and prepare files
- perform OCR on TIFF pages via Tesseract (Tess4J)
- merge PDFs and apply cryptographic signatures
- securely upload artifacts to SFTP targets
- record full audit trails and migration events

It integrates Flowable for process orchestration, uses Liquibase for schema consistency, exposes a REST API, and
provides observability via Micrometer + Prometheus/Loki.

## Features

- Java 25 first-class support (virtual threads, scoped values, ZGC tuning, faster startup)
- Spring Boot 4.x (Jakarta EE 11 baseline, modular JARs, enhanced observability)
- Strict multi-module Maven layout
- Real PostgreSQL for local development (no H2)
- Liquibase YAML changelogs (repeatable, rollback-capable)
- Flowable BPMN 2.0 workflows with custom Java delegates
- SFTP target integration (secure file delivery)
- Tesseract OCR integration (via Tess4J) for text extraction from TIFF pages
- Full observability stack (Prometheus metrics, Loki logs, Grafana dashboards)
- Aggregated JaCoCo coverage across modules (including Docker integration tests)
- GitHub Actions CI with enforced SonarCloud quality gates and Qodana static analysis
- Multi-stage Docker builds pushed to GHCR
- Interactive admin UI (React 19 + CoreUI + Vite) with real-time SSE updates, status/date filtering, and search
- REST API for migration list, aggregated stats, and per-migration detail
- SPA routing via `SpaController` (serves the React frontend from Spring Boot)

## Project Structure

```text
.
├── chainvault-migration/           # Core business logic: extraction, transformation, signing, merging, SFTP upload
├── chainvault-orchestration/       # Flowable BPMN engine, REST API, main application, delegates
│   └── src/main/java/.../
│       ├── controller/
│       │   ├── MigrationController.java   # REST: /api/migrations (list, stats, detail)
│       │   └── SpaController.java         # SPA catch-all routing → index.html
│       ├── model/entity/
│       │   ├── Migration.java             # Migration DTO
│       │   ├── MigrationDetail.java       # Extended DTO with events + download URLs
│       │   └── MigrationStats.java        # Aggregated stats DTO
│       └── workflow/service/
│           ├── AuditEventService.java     # getMigrations / getStats / getDetail
│           └── SseEmitterService.java     # SSE push (events serialised as JSON)
├── chainvault-admin-ui/            # React 19 + CoreUI admin UI (Maven module, bundled into JAR)
│   └── src/
│       ├── hooks/useMigrationEvents.ts    # SSE hook with auto-reconnect
│       └── views/pages/migration/
│           ├── Overview.tsx               # Dashboard: stats cards, live feed, table
│           └── MigrationDetailPage.tsx    # Per-migration timeline, OCR info, downloads
├── chainvault-report-aggregate/    # JaCoCo aggregated coverage reports for CI & SonarCloud
├── docker-compose.yml              # Core stack: app + postgres + sftp-test + fake-source-api
├── docker-compose-lgtm.yml         # Observability: Prometheus, Loki, Alloy, Grafana
├── env/
│   └── prometheus.yml              # Prometheus scrape configuration
└── mise.toml                       # Tool versions, environment, developer tasks
```

## Prerequisites

- Docker & Docker Compose v2+
- [mise](https://mise.jdx.dev/) — modern toolchain manager
- Git
- IDE with Spring Boot / Flowable support (IntelliJ Ultimate recommended)
- Tesseract OCR (for local development with OCR enabled): `brew install tesseract tesseract-lang`
  - Set `TESSDATA_PREFIX` to your tessdata path (e.g. `/opt/homebrew/share/tessdata`) — mise sets this automatically

One-time mise setup:

```bash
# install mise
curl https://mise.run | sh
# Add to shell (zsh/bash/fish) as shown during installation
mise doctor     # should show Java 25 + Maven ready 
```

## Quick Start

```bash
git clone https://github.com/gryphus-lab/chainvault.git
cd chainvault

mise install
mise trust

mise dev
```

After startup check:

- Health:          <http://localhost:8085/actuator/health>
- Swagger UI:      <http://localhost:8085/swagger-ui.html>
- Dashboard (SPA): <http://localhost:8085/>

## Local Development

### Common mise Commands

```bash
mise dev                    # Start postgres + app (local profile)
mise test                   # Unit + basic integration tests
mise test-docker            # Full Docker integration tests (Testcontainers)
mise verify                 # Full build + tests + JaCoCo aggregate coverage
mise docker-build           # Build & tag local Docker image
mise docker-build-versioned # Build & tag Docker image with version from POM
mise compose-down           # Docker compose down - all services                         
mise compose-down-full      # Docker compose down - all services and volumes             
mise compose-up             # Docker compose up - all services             
mise smoke-test             # Run smoke test
mise load-test              # Run load test (1000 iterations)
mise check                  # Check formatting via Spotless
mise format                 # Format source code via Spotless
```

### Observability Stack (optional but recommended)

```bash
docker compose -f docker-compose-lgtm.yml up -d
```

Access:

- Grafana:     <http://localhost:3000>  (admin/admin by default)
- Prometheus:  <http://localhost:9090>
- Loki:        <http://localhost:3100>  (via Grafana datasource)

## REST API

| Method |             Path              |                              Description                               |
|--------|-------------------------------|------------------------------------------------------------------------|
| `GET`  | `/api/migrations?limit={n}`   | List recent migrations (default 100)                                   |
| `GET`  | `/api/migrations/stats`       | Aggregated stats (total, success, failed, pending, running, last 24 h) |
| `GET`  | `/api/migrations/{id}/detail` | Full migration detail (events timeline, OCR info, download URLs)       |
| `GET`  | `/api/migrations/events`      | SSE stream of live migration events                                    |

All responses are JSON. The detail endpoint returns a `MigrationDetail` which extends `Migration` and includes:

- `events` — ordered list of `MigrationEvent` objects for the timeline
- `ocrTextPreview` — truncated OCR text preview
- `chainZipUrl` / `pdfUrl` — download links for the chain-of-custody ZIP and merged PDF

## Dashboard

The React frontend is built by `chainvault-admin-ui` (React 19 + CoreUI + Vite + TanStack Query) and served
statically by Spring Boot via `SpaController`. All SPA routes (`/`, `/migration/**`, `/dashboard`, `/overview`) are
forwarded to `index.html`.

|       View       |          Route           |                          Description                          |
|------------------|--------------------------|---------------------------------------------------------------|
| Overview         | `/`                      | Stats cards, live SSE event feed, filterable migrations table |
| Migration Detail | `/migration/{id}/detail` | Timeline, OCR breakdown, failure reason, artifact downloads   |

**Live event feed** (`useMigrationEvents` hook): subscribes to `/api/migrations/events` via SSE, buffers up to 100
events in memory, merges live status updates into the migrations table, and auto-reconnects on disconnect (3 s backoff).

## Configuration

### Secrets note

SFTP credentials, signing keys, API tokens and other sensitive values must be provided via environment variables or
mounted secrets — never commit them.

## Database & Migrations

- Engine: PostgreSQL 18 (containerized via docker-compose.yml)
- Host: localhost:5432
- Database: configured in application.yml / application-local.yml
- Migrations: Liquibase YAML
  - Location: chainvault-orchestration/src/main/resources/db/changelog/
  - Master: db.changelog-master.yaml
  - Auto-applied on startup in local profile

## Testing & Coverage

```bash
# Quick unit & basic integration tests
mise test

# Docker integration tests only (Testcontainers)
mise test-docker

# Build + tests + aggregated JaCoCo coverage (for SonarCloud / CI)
mise verify
```

### Docker Integration Tests

The `chainvault-orchestration` module contains integration test classes under `src/test/java/.../docker/` and
`src/test/java/.../controller/`:

- `DockerServicesIT` — individual service health, connectivity, and port-mapping tests
- `DockerComposeIT` — full compose-stack tests (service startup, inter-service networking)
- `MigrationControllerTest` — unit tests for `MigrationController` (stats, list, detail endpoints)
- `SpaControllerTest` — unit tests for SPA route forwarding

Run a specific class:

```bash
mvn failsafe:integration-test -Dtest=DockerServicesIT
mvn failsafe:integration-test -Dtest=DockerComposeIT
```

Coverage report location after ```mise verify```:

```text
chainvault-report-aggregate/target/site/jacoco-aggregate/index.html
```

## Docker

```bash
# Build locally
mise docker-build

# Build with version from POM
mise docker-build-versioned

# Docker compose up - all services
mise compose-up

# Docker compose down - all services
mise compose-down

# Docker compose down - all services and volumes
mise compose-down-full
```

## BPMN Workflow

The `chainvault` BPMN process (`chainvault-orchestration/src/main/resources/processes/chainvault.bpmn`) follows this
execution path:

!["Chainvault BPMN Process"](img/bpmn_process.png)

Each task has a boundary error event that routes failures to the Handle Error task, terminating with End (Failed).

## Contributing

PRs and issues are welcome. Follow the existing code style and add tests for significant changes.

## Maintainers

- gryphus-lab / <gryphus-lab@users.noreply.github.com>

---

_Generated README — edit with project-specific operational notes as needed._
