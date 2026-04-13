# Build Stage
FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /workspace

# Prepare build directories
COPY pom.xml .
COPY chainvault-admin-ui ./chainvault-admin-ui
COPY chainvault-migration ./chainvault-migration
COPY chainvault-orchestration ./chainvault-orchestration
COPY chainvault-report-aggregate ./chainvault-report-aggregate
COPY docker-resources/chainvault-app/init-scripts/setup.sh ./

# Install libatomic1 for Node.js 25.x / frontend-maven-plugin compatibility
RUN apt-get update && \
    apt-get install -y --no-install-recommends libatomic1 && \
    rm -rf /var/lib/apt/lists/*
RUN mvn -DskipTests -q package

# Runtime Stage
FROM eclipse-temurin:25-jre-noble AS runtime
ARG USERNAME=chainvault
ARG CHAINVAULT_VERSION=1.0.0-SNAPSHOT

# Setup non-root user and permissions
USER root
RUN addgroup --system "$USERNAME" && adduser --system "$USERNAME" --ingroup "$USERNAME"

# Setup script for installing Leptonica, Tesseract OCR and other dependencies
COPY --from=build /workspace/setup.sh /opt/setup.sh
RUN chmod +x /opt/setup.sh && /opt/setup.sh

# Switch to non-root user for security best practices
USER "$USERNAME"
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /workspace/chainvault-orchestration/target/chainvault-${CHAINVAULT_VERSION}.jar app.jar

# Matches server.port in chainvault-orchestration application config
EXPOSE 8085

# Healthcheck to ensure the application is running and responsive
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=3 \
  CMD ["curl", "-fsS", "http://127.0.0.1:8085/actuator/health"]

# Start the application using the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]