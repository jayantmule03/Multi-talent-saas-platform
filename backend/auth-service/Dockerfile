# ---------- Stage 1: Build ----------
# Note: common-lib must be installed to the local Maven repo first.
# From the project root: `mvn -f common-lib/pom.xml install`
# This Dockerfile builds that step in-container so the image is self-contained.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Build & install common-lib into the local repo first
COPY common-lib ./common-lib
RUN mvn -f common-lib/pom.xml -B clean install -DskipTests

# Now build auth-service itself
WORKDIR /app/auth-service
COPY auth-service/pom.xml .
RUN mvn -B dependency:go-offline
COPY auth-service/src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/auth-service/target/auth-service.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS="-Xms256m -Xmx512m"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -q -O- http://localhost:8081/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
