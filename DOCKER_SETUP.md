# Docker Setup for Digi Booking App

## Overview
This setup provides a minimal containerized environment with:
- **Backend**: Spring Boot application (port 8080)
- **Frontend**: React application served by Nginx (port 3000)
- **Database**: PostgreSQL (port 5432)
- **Monitoring**: Prometheus (port 9090) + Grafana (port 3001)

## Environment Variables
Create a `.env` file with the following variables:

```bash
# Database Configuration
POSTGRES_DB=digibooking
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password

# Spring Configuration
SPRING_PROFILE=local

# Frontend Configuration
FRONTEND_PORT=3000

# Grafana Configuration
GRAFANA_PASSWORD=admin
```

## Quick Start

1. **Build and start all services:**
   ```bash
   docker-compose up --build
   ```

2. **Access the applications:**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3001 (admin/admin)

3. **Stop all services:**
   ```bash
   docker-compose down
   ```

## Container Optimizations

- **Backend**: Uses JRE instead of JDK for smaller size
- **Frontend**: Multi-stage build with Nginx Alpine
- **Database**: PostgreSQL Alpine for minimal footprint
- **Monitoring**: Lightweight Prometheus + Grafana setup

## Health Checks
All services include health checks for better reliability and monitoring.
