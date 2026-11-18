# ==============================================================================
# Multi-stage Dockerfile pro backend aplikace Sklad (Dřevěnka s.r.o.)
# Projekt PPRO - ZS 2026/2027 (FIM UHK)
# ==============================================================================

# Fáze 1: Sestavení aplikace (Build)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Zkopírujeme pom.xml a stáhneme závislosti pro efektivní Docker layer cache
COPY pom.xml .
RUN mvn dependency:resolve -B

# Zkopírujeme zdrojové kódy a sestavíme spustitelný fat JAR balíček
COPY src ./src
RUN mvn package -DskipTests -B

# Fáze 2: Produkční runtime obraz (minimální bezpečné JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instalace curl pro kontejnerový healthcheck a vytvoření neprivilegovaného uživatele
RUN apk --no-cache add curl && \
    addgroup -S appgroup && adduser -S appuser -G appgroup

# Zkopírování vytvořeného JARu z builder fáze
COPY --from=builder /workspace/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

# Kontejnerový healthcheck – ověřuje REST endpoint /api/v1/health
HEALTHCHECK --interval=10s --timeout=5s --start-period=25s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

# Parametry JVM pro kontejner (ContainerSupport, paměťový limit, rychlý generátor náhodných čísel)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
