# Use an official minimal base image for Java 21
FROM eclipse-temurin:21-jdk-alpine

# Set workdir
WORKDIR /app

# Copy the built JAR file into the container
ARG JAR_FILE=target/inventory-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Run the app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
