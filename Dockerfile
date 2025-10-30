FROM openjdk:17-slim

WORKDIR /app

# Copy JAR file
COPY target/*.jar app.jar

# Create application properties as ROOT (before switching user)
RUN echo "management.endpoints.enabled-by-default=false" > /app/application-docker.properties && \
    echo "management.endpoint.health.enabled=false" >> /app/application-docker.properties && \
    echo "management.metrics.enable.processor=false" >> /app/application-docker.properties && \
    echo "management.metrics.enable.jvm=false" >> /app/application-docker.properties && \
    echo "management.metrics.enable.system=false" >> /app/application-docker.properties && \
    echo "spring.autoconfigure.exclude=org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration,org.springframework.boot.actuate.autoconfigure.metrics.system.SystemMetricsAutoConfiguration" >> /app/application-docker.properties

# Create non-root user and set permissions
RUN useradd -m myapp && \
    chown -R myapp:myapp /app

USER myapp

# Expose port
EXPOSE 8080

# Simple health check that tests the main application
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Run application with disabled Actuator
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=classpath:/application.properties,file:/app/application-docker.properties"]