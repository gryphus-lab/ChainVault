FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /workspace

# Cache dependencies separately
COPY pom.xml .
COPY chainvault-dashboard ./chainvault-dashboard
COPY chainvault-migration ./chainvault-migration
COPY chainvault-orchestration ./chainvault-orchestration
COPY chainvault-report-aggregate ./chainvault-report-aggregate
COPY docker-resources/chainvault-app/init-scripts/setup.sh ./
RUN mvn -DskipTests -q package

FROM eclipse-temurin:25-jre-jammy AS runtime
ARG USERNAME=chainvault
ARG CHAINVAULT_VERSION=1.0.0-SNAPSHOT

USER root
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    addgroup --system "$USERNAME" && adduser --system "$USERNAME" --ingroup "$USERNAME"

COPY --from=build /workspace/setup.sh /opt/setup.sh
RUN chmod +x /opt/setup.sh && /opt/setup.sh

USER "$USERNAME"
WORKDIR /app

COPY --from=build /workspace/chainvault-orchestration/target/chainvault-${CHAINVAULT_VERSION}.jar app.jar
# Matches server.port in chainvault-orchestration application config
EXPOSE 8085
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=3 \
  CMD ["curl", "-fsS", "http://127.0.0.1:8085/actuator/health"]
ENTRYPOINT ["java", "-jar", "app.jar"]