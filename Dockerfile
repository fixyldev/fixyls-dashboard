##### BUILD STAGE #####
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Copy build configuration
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

# Fetch dependencies
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Build jar
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

##### RUN STAGE #####
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Set executing user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
