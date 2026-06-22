# Stage 1: Build the Gradle project
FROM gradle:8-jdk21 AS build
WORKDIR /app

# 1. Copy the top-level Gradle wrapper and configuration files
COPY gradlew build.gradle* settings.gradle* gradle.properties* ./
COPY gradle ./gradle

# 2. Copy the source code for the backend modules
COPY shared ./shared
COPY server ./server

# 3. GENERATE THE KEYSTORE FILE AUTOMATICALLY
RUN keytool -genkeypair -alias local-backend -keyalg RSA -keysize 4096 -validity 365 -keystore keystore.jks -storepass changeit -keypass changeit -dname "CN=localhost, OU=Dev, O=UniTodo, L=Munich, C=DE" -ext "SAN=dns:localhost,ip:127.0.0.1"

# 4. Use installDist to package the app
RUN ./gradlew :server:installDist -x test --no-daemon

# Stage 2: Create the lightweight runtime container
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 5. Copy the compiled server application
COPY --from=build /app/server/build/install/server /app/

# 6. Copy the generated keystore file into the final container
COPY --from=build /app/keystore.jks /app/keystore.jks

# Expose the port your custom server listens on
EXPOSE 6767

# Run the server
CMD ["sh", "-c", "printenv > .env && ./bin/server"]