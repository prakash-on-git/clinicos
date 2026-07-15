# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Maven 3.9 + JDK 21 Alpine — compiles the project and produces the fat JAR.
# Using a separate build stage keeps the final image lean (no Maven cache, no JDK).
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy POM first so Docker caches the dependency layer separately from source.
# Rebuilds only invalidate the dependency layer when pom.xml actually changes.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build (skip tests — tests run in CI, not during image build)
COPY src src
RUN mvn package -DskipTests -q

# Compile the GnuCOBOL billing engine (see src/main/cobol/BillingCalc.cbl)
RUN apk add --no-cache gnucobol
COPY scripts/compile-cobol.sh scripts/compile-cobol.sh
RUN ./scripts/compile-cobol.sh /build/cobol-out

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
# Minimal JRE 21 Alpine — only what's needed to run the JAR.
FROM eclipse-temurin:21-jre-alpine

# gnucobol provides libcob.so, needed at runtime by the compiled billing_calc binary
RUN apk add --no-cache gnucobol

# Create a non-root user/group for security — never run as root in production
RUN addgroup -S clinicos && adduser -S clinicos -G clinicos

WORKDIR /app

# Copy the fat JAR and the compiled COBOL billing engine from the builder stage
COPY --from=builder /build/target/*.jar app.jar
COPY --from=builder /build/cobol-out/billing_calc cobol/billing_calc
ENV COBOL_BINARY_PATH=/app/cobol/billing_calc

# Give ownership to the non-root user
RUN chown -R clinicos:clinicos /app

USER clinicos

EXPOSE 8080

# Health check via the OpenAPI docs endpoint (lightweight, no auth required)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/api-docs > /dev/null 2>&1 || exit 1

# -XX:+UseContainerSupport  — JVM respects Docker CPU/memory limits (JDK 11+)
# -XX:MaxRAMPercentage=75.0 — JVM heap uses up to 75% of container memory
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
