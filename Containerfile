# Stage 1: Build the application
# Use Docker format for compatibility with HEALTHCHECK
FROM docker.io/eclipse-temurin:21-jdk-alpine AS build

# Install Maven
RUN apk add --no-cache maven

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy pom.xml and package.json first for better caching
COPY pom.xml .
COPY package.json package-lock.json ./

# Copy source code
COPY src ./src
COPY tsconfig.json webpack.config.js jest.config.js jest.setup.tsx ./

# Build the application (both backend and frontend)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
# Use Docker format for compatibility with HEALTHCHECK
FROM docker.io/eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Install curl for health check
RUN apk add --no-cache curl

# Expose application port
EXPOSE 8080

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
