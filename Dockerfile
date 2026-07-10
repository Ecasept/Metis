# Container for budling
FROM gradle:8-jdk21 AS build
WORKDIR /app

# Copy files
COPY gradlew build.gradle* settings.gradle* gradle.properties* ./
COPY gradle ./gradle

COPY shared ./shared
COPY server ./server

RUN ./gradlew :server:installDist -x test --no-daemon

# Container for running
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy build output
COPY --from=build /app/server/build/install/server /app/

EXPOSE 6767
CMD ["sh", "-c", "printenv > .env && ./bin/server"]