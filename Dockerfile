# Stage 1: Build the Gradle project
FROM gradle:8-jdk21 AS build
WORKDIR /app

# 1. Copy the top-level Gradle wrapper and configuration files
COPY gradlew build.gradle* settings.gradle* gradle.properties* ./
COPY gradle ./gradle

# 2. Copy the source code for the backend modules
# (If server depends on shared, shared must be copied too)
COPY shared ./shared
COPY server ./server

# 3. Build ONLY the server module and skip tests
RUN ./gradlew :server:bootJar --no-daemon || ./gradlew :server:build -x test --no-daemon

# Stage 2: Create the lightweight runtime container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 4. Extract the JAR specifically from the server module's build directory
COPY --from=build /app/server/build/libs/*-SNAPSHOT.jar app.jar || COPY --from=build /app/server/build/libs/*.jar app.jar

# Expose the port your custom server listens on
EXPOSE 8080

# Run the server
CMD ["java", "-jar", "app.jar"]