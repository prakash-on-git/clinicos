# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Maven 3.9 + JDK 21 on Ubuntu Jammy (not Alpine) — Alpine's package repos only
# ship gnucobol on the "edge" branch, not on any stable release, so `apk add
# gnucobol` fails at build time. Ubuntu's universe component carries it on
# jammy, so we use the Ubuntu-based temurin image and enable universe below.
FROM maven:3.9-eclipse-temurin-21-jammy AS builder

WORKDIR /build

# Copy POM first so Docker caches the dependency layer separately from source.
# Rebuilds only invalidate the dependency layer when pom.xml actually changes.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build (skip tests — tests run in CI, not during image build)
COPY src src
RUN mvn package -DskipTests -q

# Compile the GnuCOBOL billing engine (see src/main/cobol/BillingCalc.cbl).
# gnucobol lives in Ubuntu's universe component, which isn't enabled by
# default in the base image, so add it explicitly before installing.
RUN echo "deb http://archive.ubuntu.com/ubuntu jammy universe" > /etc/apt/sources.list.d/universe.list \
    && apt-get update && apt-get install -y --no-install-recommends gnucobol \
    && rm -rf /var/lib/apt/lists/*
COPY scripts/compile-cobol.sh scripts/compile-cobol.sh
RUN ./scripts/compile-cobol.sh /build/cobol-out

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
# Minimal JRE 21 on Ubuntu Jammy — only what's needed to run the JAR.
FROM eclipse-temurin:21-jre-jammy

# gnucobol provides libcob.so (needed at runtime by the compiled billing_calc
# binary) and wget (used by the HEALTHCHECK below) — neither ships by default,
# and gnucobol needs the universe component enabled (see builder stage above).
RUN echo "deb http://archive.ubuntu.com/ubuntu jammy universe" > /etc/apt/sources.list.d/universe.list \
    && apt-get update && apt-get install -y --no-install-recommends gnucobol wget \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root user/group for security — never run as root in production
RUN groupadd -r clinicos && useradd -r -g clinicos clinicos

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
