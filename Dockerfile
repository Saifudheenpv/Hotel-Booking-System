FROM openjdk:17-jre-slim

WORKDIR /app

# Copy JAR file
COPY target/*.jar app.jar

# Create non-root user
RUN useradd -m myapp
USER myapp

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]