# ================================
# BUILD STAGE
# ================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source
COPY src ./src

# Build jar
RUN mvn package -DskipTests -B

# ================================
# RUNTIME STAGE
# ================================
FROM eclipse-temurin:21-jre-alpine

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Create logs directory AFTER user exists
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

# Copy jar
COPY --from=build --chown=appuser:appgroup /app/target/*.jar app.jar

USER appuser
EXPOSE 8080

# Health check (requires actuator)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD wget --spider -q http://localhost:8080/actuator/health || exit 1

# Application Startup
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]