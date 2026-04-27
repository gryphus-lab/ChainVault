# Build Stage
FROM maven:3-eclipse-temurin-26 AS build
WORKDIR /workspace

# Copy pom first for better layer caching (dependencies layer rarely changes)
COPY pom.xml .

# Copy source modules
COPY chainvault-admin-ui ./chainvault-admin-ui
COPY chainvault-migration ./chainvault-migration
COPY chainvault-orchestration ./chainvault-orchestration
COPY chainvault-report-aggregate ./chainvault-report-aggregate
COPY docker-resources/chainvault-app/init-scripts/setup.sh ./

# Install libatomic1 for Node.js 25.x / frontend-maven-plugin compatibility
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      libatomic1=14.2.0-4ubuntu2~24.04.1 && \
    rm -rf /var/lib/apt/lists/*

# Build application with optimized settings
RUN mvn -DskipTests -q -B package

# Runtime Stage
FROM eclipse-temurin:25-jre-noble AS runtime
ARG USERNAME=chainvault
ARG CHAINVAULT_VERSION=1.0.0-SNAPSHOT

# Setup non-root user and permissions
USER root
RUN addgroup --system "$USERNAME" && adduser --system "$USERNAME" --ingroup "$USERNAME"

# Setup script for installing Leptonica, Tesseract OCR and other dependencies
COPY --from=build --chown=$USERNAME:$USERNAME /workspace/setup.sh /opt/setup.sh
RUN chmod +x /opt/setup.sh && /opt/setup.sh

WORKDIR /app

# Copy the built JAR from the build stage with explicit ownership
COPY --from=build --chown=$USERNAME:$USERNAME /workspace/chainvault-orchestration/target/chainvault-${CHAINVAULT_VERSION}.jar app.jar

# Create directory for temporary/log files (owned by app user)
RUN chown $USERNAME:$USERNAME /app

# Switch to non-root user for security best practices
USER $USERNAME

# Matches server.port in chainvault-orchestration application config
EXPOSE 8085

# Healthcheck to ensure the application is running and responsive
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=3 \
  CMD ["curl", "-fsS", "http://127.0.0.1:8085/actuator/health"]

# Start the application using JVM optimizations for containers
ENTRYPOINT ["java", \
  "-XX:+UseG1GC", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseStringDeduplication", \
  "-XX:+ParallelRefProcEnabled", \
  "-Dcom.sun.management.jmxremote=false", \
  "-Dfile.encoding=UTF-8", \
  "-jar", "app.jar"]
