# Docker Integration Tests

This directory contains comprehensive integration tests for the Docker services defined in `docker-compose.yml`.

## Test Files

### 1. `DockerServicesIntegrationIT.java`

#### Unit-level integration tests for individual Docker services

Tests the health, availability, and basic connectivity of each service in isolation:

- **PostgreSQL Tests**
  - `testPostgresHealthCheck()`: Validates database connectivity and basic SQL query execution
  - `testPostgresVersionCompatibility()`: Confirms PostgreSQL 16 is running as expected
  - `testPostgresPortMapping()`: Verifies port mapping is correctly configured
  - `testPostgresConnectionPooling()`: Tests multiple concurrent database connections
  - `testPostgresDatabaseInitialization()`: Verifies database creation and initialization

- **SFTP Service Tests**
  - `testSftpServiceAvailability()`: Checks SFTP container is running and port is exposed
  - `testSftpUserAccess()`: Tests file creation, listing, and deletion in SFTP server
  - `testSftpPortMapping()`: Validates dynamic port allocation for SFTP
  - `testSftpUploadDirectory()`: Confirms upload directory exists and is accessible

- **API Service Tests**
  - `testApiServiceAvailability()`: Verifies API container is running
  - `testApiResponseFormat()`: Validates API responds to HTTP requests with correct status code

- **General Tests**
  - `testServiceIsolation()`: Ensures each service runs in its own isolated container
  - `testServiceMemoryAndResourceUsage()`: Confirms all containers are properly instantiated

### 2. `DockerComposeIT.java`

#### Docker Compose orchestration tests

Tests the entire docker-compose stack to ensure services work together:

- `testAllServicesStart()`: Validates all services defined in docker-compose.yml start successfully
- `testPostgresServiceAccessibility()`: Tests postgres service is accessible within the compose network
- `testSftpServiceAccessibility()`: Tests SFTP service is accessible within the compose network
- `testApiServiceAccessibility()`: Tests API service is accessible within the compose network
- `testServiceNetworkConnectivity()`: Validates services can communicate with each other
- `testServiceDefinitions()`: Confirms all expected services are defined
- `testPortMapping()`: Tests port mapping for all services

**Note:** This test requires `docker-compose.yml` to be available at the project root.

### 3. `DockerServicesE2EIT.java`

#### End-to-end integration tests

Tests realistic scenarios where all services work together in a unified system:

- **Service Health Tests**
  - `testAllServicesHealthy()`: Verifies all services are running
  - `testServiceStability()`: Confirms services remain stable over time

- **Connectivity Tests**
  - `testPostgresConnectivity()`: Tests actual database connections
  - `testApiConnectivity()`: Tests HTTP API access and response handling
  - `testSftpConnectivity()`: Tests SFTP filesystem access

- **Capability Tests**
  - `testSftpUploadCapability()`: Tests file upload/download functionality
  - `testPostgresConcurrentConnections()`: Tests multi-threaded database access
  - `testApiHttpAccess()`: Tests HTTP GET requests with response parsing
  - `testDataPersistence()`: Tests data persistence in SFTP

- **Reliability Tests**
  - `testPostgresDatabaseInitialization()`: Confirms database schema initialization
  - `testDynamicSftpPortAllocation()`: Validates dynamic port allocation works
  - `testApiTimeoutHandling()`: Tests graceful timeout handling for HTTP requests

## Running the Tests

### Run all Docker tests

```bash
mvn test -Dtest=Docker*
```

### Run specific test class

```bash
# Individual service tests
mvn test -Dtest=DockerServicesIntegrationIT

# Docker Compose orchestration tests
mvn test -Dtest=DockerComposeIT

# End-to-end tests
mvn test -Dtest=DockerServicesE2EIT
```

### Run with coverage

```bash
mvn test -Dtest=Docker* -Pcoverage
```

## Test Configuration

### Docker Requirements

- Docker daemon must be running and accessible
- Docker socket should be mounted/accessible to the test runner
- Tests use TestContainers library for container management

### Dependencies

- **testcontainers**: 2.0.3 (already in pom.xml)
- **testcontainers-junit-jupiter**: 2.0.3
- **testcontainers-postgresql**: 2.0.3
- **awaitility**: For polling/waiting in async scenarios
- **httpClient**: Built-in Java 11+ HTTP client for API tests

### Port Allocation

- **PostgreSQL**: Dynamically allocated (5432 internal)
- **SFTP**: Dynamically allocated (22 internal) - now using dynamic local port per docker-compose update
- **API**: Port 9091 (fixed)

## Service Descriptions

### PostgreSQL 16-Alpine

- **Image**: `postgres:16-alpine`
- **Purpose**: Primary data storage for the application
- **Initialization**: Scripts from `src/main/resources/db/init-scripts` are automatically run
- **Exposed Port**: 5432 (internal)

### SFTP Server

- **Image**: `atmoz/sftp:latest`
- **Purpose**: SFTP file transfer endpoint for document uploads
- **Credentials**:
  - Username: `testuser`
  - Password: `testpass123`
  - Upload directory: `/home/testuser/upload`
- **Exposed Port**: 22 (internal, dynamic local port allocation)

### Fake Source API

- **Image**: `node:25-alpine`
- **Purpose**: Mock REST API for document source data (using json-server)
- **Data**: `src/test/resources/db.json` and `src/test/resources/static`
- **Exposed Port**: 9091

## Troubleshooting

### Tests fail with "Docker daemon not responding"

- Ensure Docker is running: `docker ps`
- Check Docker socket permissions if running in a container

### Tests timeout waiting for services

- Increase timeout values in `waitingFor()` calls
- Check service logs: `docker logs <container-name>`

### Port conflicts

- Ensure ports 5432, 9091 are available or not already in use
- SFTP uses dynamic port allocation to avoid conflicts

### TestContainers not found

- Ensure Maven dependencies are properly installed
- Run `mvn clean install` to refresh dependencies

## CI/CD Integration

These tests are designed to run in:

- Local development environments
- GitHub Actions workflows
- Docker-based CI systems
- Kubernetes test pods

They automatically handle container lifecycle management and cleanup.

## Future Enhancements

- [ ] Add health check endpoint monitoring
- [ ] Add service performance/load testing
- [ ] Add database migration testing
- [ ] Add SFTP performance testing
- [ ] Add security/authentication testing
- [ ] Add cross-service communication tests
- [ ] Add chaos engineering tests (container failures)
