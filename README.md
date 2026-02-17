# FINMA / GeBüV Compliant Document Migration Tool

A secure, auditable migration tool that transfers legacy archived documents (per-page TIFF containers) to a modern target archive while preserving full chain of custody.

- Extracts TIFF pages from ZIP containers via REST API
- Creates immutable chain-of-custody ZIP + manifest
- Merges pages into a single lossless PDF using Apache PDFBox
- Generates minimal metadata XML
- Uploads all artifacts via SFTP to the target archive
- Runs as background jobs with JobRunr (dashboard included)

Designed to meet **GeBüV** integrity, traceability and procedural documentation requirements (Art. 6–9) and FINMA expectations for data handling in financial institutions.

## Features

- **Chain of custody** — original TIFFs preserved in ZIP + SHA-256 hashes
- **Lossless PDF** — single merged file created with PDFBox
- **Auditable** — JobRunr dashboard + structured logging
- **Secure** — SFTP with optional key authentication
- **Scalable** — parallel background processing
- **Restartable** — failed jobs can be retried via dashboard

## Tech Stack

- Java 21
- Spring Boot 3.3+
- JobRunr 8.3+ (background jobs + dashboard)
- Apache PDFBox 3.0+ (TIFF → PDF merging)
- Spring Integration SFTP (Apache Mina SSHD)
- Jackson XML (metadata generation)
- RestClient (source legacy archive API)
- Lombok, SLF4J, JUnit 5 + Mockito

## Prerequisites

- Java 21 (Eclipse Temurin / Corretto / Oracle)
- Maven 3.8+
- Target SFTP server credentials
- Access to legacy archive REST API (with token)

## Quick Start

1. Clone the repository

```bash
git clone <your-repo-url>
cd finma-migration