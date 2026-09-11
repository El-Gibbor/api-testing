# Runs the REST Assured test suite in an isolated, reproducible environment.
#
# Build:
#   docker build -t api-testing .
#
# Run (mount a host directory to retrieve the Allure results afterwards):
#   docker run --rm -v "$(pwd)/target/allure-results:/app/target/allure-results" api-testing
#
# Target a different environment without rebuilding:
#   docker run --rm -e BASE_URI=https://staging.example.com api-testing
#
# Multi-stage build: the "builder" stage resolves dependencies and pre-compiles the test
# sources against a warm local Maven repo; the final stage reuses that repo and compiled
# classes so `mvn test` at container start doesn't re-resolve anything from the network.
# Both stages use the Alpine variant of the Maven/Temurin image (~3x smaller than the
# Debian-based default) since nothing here needs more than a JDK, Maven, and libc.

FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Cache dependencies in their own layer, invalidated only when pom.xml changes.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B test-compile

FROM maven:3.9.9-eclipse-temurin-17-alpine

WORKDIR /app

COPY --from=builder /root/.m2 /root/.m2
COPY --from=builder /app /app

CMD ["mvn", "-B", "test"]
