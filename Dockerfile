
# Use Java 21 runtime
FROM eclipse-temurin:21-jre

# Set working directory inside the container
WORKDIR /app

# Copy the packaged Spring Boot application
COPY target/shopease-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8080

# Start ShopEase
ENTRYPOINT ["java", "-jar", "app.jar"]
