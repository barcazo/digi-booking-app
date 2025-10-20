# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS build

# Install Maven
RUN apk add --no-cache maven

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
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Install curl for health check
RUN apk add --no-cache curl

# Expose application port
EXPOSE 8083

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

# Run the application with gateway service profile
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=gateway-service", "--server.port=8083"]