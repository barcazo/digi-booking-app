# Running the Application with Podman

This document provides instructions for running the application using Podman instead of Docker.

## Prerequisites

- [Podman](https://podman.io/getting-started/installation) installed on your system
- [Podman Compose](https://github.com/containers/podman-compose) for running multi-container applications

## Installation

### Installing Podman on Windows

1. Install Podman Desktop from the [official website](https://podman-desktop.io/downloads).
2. During installation, Podman Desktop will install the required components including Podman.
3. After installation, open Podman Desktop and follow the setup wizard to configure Podman.

### Installing Podman Compose

```powershell
pip3 install podman-compose
```

## Running the Application

1. Set up environment variables (if not already defined in a .env file):

```powershell
# Example environment variables
$env:POSTGRES_DB = "digibooking"
$env:POSTGRES_USER = "postgres"
$env:POSTGRES_PASSWORD = "password"
$env:SPRING_PROFILE = "local"
$env:FRONTEND_PORT = "3000"
$env:KEYCLOAK_USER = "admin"
$env:KEYCLOAK_PASSWORD = "P4ssword!"
```

2. Run the application using podman-compose:

```powershell
podman-compose up -d
```

This command will start all services defined in the compose.yml file in detached mode.

3. To check the status of the containers:

```powershell
podman-compose ps
```

4. To view logs from a specific service:

```powershell
podman-compose logs <service-name>
```

5. To stop all services:

```powershell
podman-compose down
```

## Accessing the Application

- Frontend: http://localhost:3000
- API Gateway: https://localhost/api/
- Keycloak Admin Console: http://localhost:8085
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (default credentials: admin/admin)

## Troubleshooting

If you encounter any issues with Podman, try the following:

1. Check if Podman is running correctly:

```powershell
podman info
```

2. If you're having network issues, try resetting the Podman network:

```powershell
podman network rm podman
podman network create podman
```

3. For Windows-specific issues:
   - Make sure Podman Machine is running: `podman machine list`
   - If needed, start the Podman Machine: `podman machine start`
   - For WSL2 backend issues, try restarting WSL: `wsl --shutdown` and then start Podman Desktop again

4. For permission issues, make sure you're running PowerShell with appropriate privileges (Run as Administrator if needed).

5. If containers fail to start, check the logs:

```powershell
podman-compose logs
```

## Notes on Podman vs Docker

Podman is designed to be a drop-in replacement for Docker, but there are some differences:

- Podman runs containers as the current user by default, not as root
- Podman doesn't use a daemon, which can improve security
- Some Docker-specific features might not be available in Podman

For more information, refer to the [Podman documentation](https://docs.podman.io/).
