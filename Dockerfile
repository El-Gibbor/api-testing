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
FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /app

# Cache dependencies in their own layer, invalidated only when pom.xml changes.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

CMD ["mvn", "-B", "test"]
