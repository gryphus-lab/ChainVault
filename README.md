# chainvault

**Lightweight orchestration & migration engine for secure document processing**  
Used by Gryphus Lab to coordinate extraction, transformation, signing, merging and secure SFTP delivery of documents.

[![Java 25](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4.0+-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-managed-2962FF)](https://www.liquibase.org/)
[![Flowable](https://img.shields.io/badge/orchestrated%20with-Flowable-0072C6)](https://www.flowable.com/)
[![mise](https://img.shields.io/badge/managed%20with-mise-6f42c1)](https://mise.jdx.dev/)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=gryphus-lab_chainvault&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=gryphus-lab_chainvault)
[![GitHub Actions CI](https://github.com/gryphus-lab/chainvault/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/gryphus-lab/chainvault/actions/workflows/ci.yml)

## At a Glance

|      Aspect       |             Technology              |
|-------------------|-------------------------------------|
| Language          | Java 25                             |
| Framework         | Spring Boot 4                       |
| Orchestration     | Flowable (BPMN 2.0)                 |
| Database          | PostgreSQL 17 (Docker)              |
| Schema Migrations | Liquibase (YAML)                    |
| Developer Tooling | mise                                |
| Observability     | Prometheus + Loki + Grafana + Alloy |
| CI / Quality      | GitHub Actions + SonarCloud         |
| Testing           | JUnit 5 + Testcontainers            |
| API Documentation | springdoc OpenAPI / Swagger UI      |

## Overview

Chainvault is a **Spring Boot 4 + Java 25** orchestration service that executes BPMN workflows to:

- extract & hash documents
- transform metadata and prepare files
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
- Full observability stack (Prometheus metrics, Loki logs, Grafana dashboards)
- Aggregated JaCoCo coverage across modules (including Docker integration tests)
- GitHub Actions CI with enforced SonarCloud quality gates on new code
- Multi-stage Docker builds pushed to GHCR

## Project Structure

```
.
├── chainvault-migration/           # Core business logic: extraction, transformation, signing, merging, SFTP upload
├── chainvault-orchestration/       # Flowable BPMN engine, REST API, main application, delegates
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

* Health:     http://localhost:8085/actuator/health
* Swagger UI: http://localhost:8085/swagger-ui.html

## Local Development

### Common mise Commands

```bash
mise dev                # Start postgres + app (local profile)
mise test               # Unit + basic integration tests
mise test-docker        # Full Docker integration tests (Testcontainers)
mise verify             # Full build + tests + JaCoCo aggregate coverage
mise docker-build       # Build & tag local Docker image
```

### Observability Stack (optional but recommended)

```bash
docker compose -f docker-compose-lgtm.yml up -d
```

Access:

* Grafana:     http://localhost:3000  (admin/admin by default)
* Prometheus:  http://localhost:9090
* Loki:        http://localhost:3100  (via Grafana datasource)

## Configuration

### Secrets note

SFTP credentials, signing keys, API tokens and other sensitive values must be provided via environment variables or
mounted secrets — never commit them.

## Database & Migrations

* Engine: PostgreSQL 17 (containerized via docker-compose.yml)
* Host: localhost:5432
* Database: configured in application.yml / application-local.yml
* Migrations: Liquibase YAML
  * Location: chainvault-orchestration/src/main/resources/db/changelog/
  * Master: db.changelog-master.yaml
  * Auto-applied on startup in local profile

## Testing & Coverage

```bash
# Quick unit & basic integration tests
mise test

# Full suite including Docker-based integration tests
mise test-docker

# Build + tests + aggregated JaCoCo coverage (for SonarCloud / CI)
mise verify
```

Coverage report location after ```mise verify```:

```text
chainvault-report-aggregate/target/site/jacoco-aggregate/index.html
```

## Docker

```bash
# Build locally
mise docker-build

# Docker compose up - all services
mise compose-up

# Docker compose down - all services
mise comoose-down
```

## Contributing

PRs and issues are welcome. Follow the existing code style and add tests for significant changes.

## Maintainers

- gryphus-lab / <gryphus-lab@users.noreply.github.com>

---

_Generated README — edit with project-specific operational notes as needed._
