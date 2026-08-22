# ---------- Stage 1: Build ----------
# Build context must be the project root (needs access to ../common-lib)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY common-lib ./common-lib
RUN mvn -f common-lib/pom.xml -B clean install -DskipTests

WORKDIR /app/project-service
COPY project-service/pom.xml .
RUN mvn -B dependency:go-offline
COPY project-service/src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/project-service/target/project-service.jar app.jar

EXPOSE 8083

ENV JAVA_OPTS="-Xms256m -Xmx512m"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -q -O- http://localhost:8083/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
