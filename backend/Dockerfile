# ==============================================================================
# Multi-stage Dockerfile pro backend aplikace Sklad (Dřevěnka s.r.o.)
# Projekt PPRO - ZS 2026/2027
# ==============================================================================

# Fáze 1: Sestavení aplikace (Build)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Nejprve zkopírujeme pom.xml pro zacachování závislostí
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Zkopírujeme zdrojové kódy a zkompilujeme finální JAR balíček
COPY src ./src
RUN mvn clean package -DskipTests -B

# Fáze 2: Produkční runtime obraz (JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Vytvoření neprivilegovaného systémového uživatele pro bezpečný běh
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Zkopírování vytvořeného spustitelného JAR souboru z builder fáze
COPY --from=builder /workspace/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

# Parametry JVM optimalizované pro kontejnerizované prostředí
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

