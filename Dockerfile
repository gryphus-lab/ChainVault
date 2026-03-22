FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /workspace

# Cache dependencies separately
COPY pom.xml .
COPY chainvault-migration ./chainvault-migration
COPY chainvault-orchestration ./chainvault-orchestration
COPY chainvault-report-aggregate ./chainvault-report-aggregate
COPY chainvault-archrules ./chainvault-archrules
COPY docker-resources/chainvault-app/init-scripts/setup.sh ./
RUN mvn -DskipTests -q package

FROM eclipse-temurin:25-jre-jammy AS runtime
ARG USERNAME=chainvault
ARG CHAINVAULT_VERSION=1.0.0-SNAPSHOT

RUN addgroup --system "$USERNAME" && adduser --system "$USERNAME" --ingroup "$USERNAME"
COPY --from=build /workspace/setup.sh /opt/setup.sh
RUN chmod +x /opt/setup.sh && /opt/setup.sh

USER "$USERNAME"
WORKDIR /app
COPY --from=build /workspace/chainvault-orchestration/target/chainvault-${CHAINVAULT_VERSION}.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]