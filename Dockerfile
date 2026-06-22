# Stage 1: Build the Gradle project
FROM gradle:8-jdk21 AS build
WORKDIR /app

# Copy gradle files first to leverage Docker caching for dependencies
COPY gradlew build.gradle* settings.gradle* gradle.properties* ./
COPY gradle ./gradle

# Download dependencies (this saves time on future builds)
RUN ./gradlew dependencies --no-daemon || true

# Copy your actual source code
COPY src ./src

# Build the application and skip tests to speed up deployment
RUN ./gradlew bootJar --no-daemon || ./gradlew build -x test --no-daemon

# Stage 2: Create the lightweight runtime container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the compiled JAR file from the build stage
# Note: Fat JARs usually end up in build/libs/
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar || COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port your custom server listens on
EXPOSE 8080

# Run the server
CMD ["java", "-jar", "app.jar"]