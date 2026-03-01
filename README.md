# chainvault

Lightweight orchestration service for document migration and processing used by Gryphus Lab.

## Overview

`chainvault` is a Spring Boot-based migration/orchestration application that coordinates document extraction,
transformation, signing, merging and secure transfer. It exposes BPMN workflows (see the process definition), integrates
with SFTP targets, and records migration audit/events via database migrations.

Key responsibilities:

- Extract and hash documents
- Transform metadata and prepare files
- Merge PDFs and sign documents
- Upload artifacts to SFTP targets
- Record audit and event logs via Liquibase migrations

## Repo layout

- `chainvault-migration/` — migration service module: domain, extraction/transform/merge/sign/upload logic, SFTP and
  REST client config
- `chainvault-orchestration/` — Flowable BPMN orchestration module: process definition, delegates, REST API, main
  application
- `chainvault-report-aggregate/` — JaCoCo aggregate module combining coverage from `chainvault-migration` and
  `chainvault-orchestration` (unit + integration/Docker tests), used for CI/Sonar
- `docker-compose.yml` — main app stack (chainvault, postgres, sftp-test, fake-source-api)
- `docker-compose-monitoring.yml` — observability stack (Prometheus, Loki, Alloy, Grafana)
- `env/prometheus.yml` — Prometheus scrape config
- `run_chainvault_docker.sh` — script to build and run app + monitoring with Docker Compose

## Prerequisites

- Java 21+ (or the JDK version required by your environment)
- Git
- Docker & Docker Compose (optional, for containerized runs)

The project includes the Maven wrapper (`mvnw`), so a local Maven install is not required.

## Build

From the repository root:

```bash
./mvnw clean package -DskipTests
```

This produces a runnable JAR `chainvault-<version>.jar` in both `target/` (project root) and
`chainvault-orchestration/target/` (e.g. `chainvault-1.0.0-SNAPSHOT.jar`).

## Run

Run the packaged JAR:

```bash
java -jar target/chainvault-<version>.jar
```

Or use Docker Compose to start services as defined in `docker-compose.yml`:

```bash
docker-compose up --build
```

To build and run the app stack together with the monitoring stack (Prometheus, Loki, Alloy, Grafana):

```bash
./run_chainvault_docker.sh
```

This runs `docker-compose -f docker-compose-monitoring.yml -f docker-compose.yml up -d` after a clean build.

## Configuration

- Main Spring configuration: `chainvault-orchestration/src/main/resources/application.yml`.
- Mapping and process definitions: `mapping-config.yml`, `processes/chainvault.bpmn` (in orchestration module).
- Database migrations are in `chainvault-orchestration/src/main/resources/db/migration` and are applied via Liquibase on
  startup (see `db/changelog/db.changelog-master.yaml`).
- OpenAPI (springdoc) is configured in `application.yml`; Swagger UI is at `/swagger-ui.html` when the app is running.

## Database

The repository contains SQL migration scripts under `src/main/resources/db/migration`. These are applied by Liquibase
using the master changelog at `src/main/resources/db/changelog/db.changelog-master.yaml`. A database must be configured
in `application.yml` (spring.datasource settings). Spring Boot's auto‑configuration creates a `DataSource` bean
automatically, so no custom config class is required.

## Testing

Run unit and integration tests with:

```bash
./mvnw test
```

Docker integration tests (Testcontainers) validate the full stack and individual services; see
`chainvault-orchestration/src/test/java/ch/gryphus/chainvault/docker/README.md`. Run them with:

```bash
./mvnw test -Dtest=Docker*
```

### Test coverage (JaCoCo, multi-module)

To generate JaCoCo coverage reports (including integration/Docker tests) across all modules, use the `coverage` profile
from the project root:

```bash
./mvnw verify -Pcoverage
```

This will:

- Attach JaCoCo agents for both unit and integration tests in each module
- Generate per-module reports, and
- Produce an aggregated report in the `chainvault-report-aggregate` module (HTML + XML) suitable for CI/Sonar
  consumption.

## Observability

- **Prometheus**: Metrics are exposed at `/prometheus` (Micrometer). Use `docker-compose-monitoring.yml` to run
  Prometheus, Loki, Alloy, and Grafana; scrape config is in `env/prometheus.yml`.
- **OpenAPI**: REST API docs and Swagger UI at `/swagger-ui.html` (springdoc).

## Useful files

- Application entry: `chainvault-orchestration/src/main/java/ch/gryphus/chainvault/MigrationApplication.java`
- BPMN process: `chainvault-orchestration/src/main/resources/processes/chainvault.bpmn`
- Liquibase changelog: `chainvault-orchestration/src/main/resources/db/changelog/db.changelog-master.yaml`
- OpenAPI config: `chainvault-orchestration/src/main/java/ch/gryphus/chainvault/config/OpenApiConfig.java`
- Migration service: `chainvault-migration/src/main/java/ch/gryphus/chainvault/service/MigrationService.java`

## Contributing

PRs and issues are welcome. Follow the existing code style and add tests for significant changes.

## Maintainers

- gryphus-lab / gryphus-lab@users.noreply.github.com

---
_Generated README — edit with project-specific operational notes as needed._