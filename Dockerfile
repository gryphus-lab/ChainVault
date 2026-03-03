FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY ./pom.xml ./mvnw ./
COPY chainvault-migration ./chainvault-migration
COPY chainvault-orchestration ./chainvault-orchestration
COPY chainvault-report-aggregate ./chainvault-report-aggregate
RUN mvn -DskipTests -q package

FROM eclipse-temurin:25-jdk-alpine AS runtime
ARG USERNAME=chainvault
ARG USER_UID=1000
ARG USER_GID=$USER_UID
ARG CHAINVAULT_VERSION=1.0.0-SNAPSHOT

# Create the user
RUN addgroup -S "$USERNAME" \
    && adduser -S "$USERNAME" -G "$USERNAME"

USER $USERNAME
WORKDIR /app
COPY --from=build /workspace/chainvault-orchestration/target/chainvault-${CHAINVAULT_VERSION}.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
