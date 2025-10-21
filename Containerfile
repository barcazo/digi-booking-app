# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS build

# Install Maven and Node.js LTS from Alpine's package repo
RUN apk add --no-cache maven nodejs npm

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy dependency files first for better caching
COPY pom.xml .
COPY package.json package-lock.json ./

# Download Maven dependencies first
RUN mvn dependency:go-offline -B -T 4

# Install frontend dependencies
RUN npm ci --prefer-offline --no-audit --no-fund

# Copy source code and config files
COPY src ./src
COPY tsconfig.json webpack.config.js jest.config.js jest.setup.tsx ./

# Build the application (both backend and frontend) with parallel builds
RUN mvn package -DskipTests -B -T 4 -Dmaven.test.skip=true

# Stage 2: Run the application
FROM eclipse-temurin:21-jdk-alpine

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
