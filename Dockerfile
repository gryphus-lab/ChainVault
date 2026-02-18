# Use official Eclipse Temurin JDK 21 base image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven build artifacts
COPY target/chainvault-0.0.1-SNAPSHOT.jar app.jar

# Expose Spring Boot default port + JobRunr dashboard
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]