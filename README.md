# Booking Api System

This app was created with Spring Initializer using Spring Boot 3.5.6 - tips on working with the code [can be found here](https://spring.io/).

## Development

When starting the application `podman-compose up` is called and the app will connect to the contained services. [Podman](https://podman.io/getting-started/installation) must be available on the current system. See the `podman-instructions.md` file for detailed instructions on setting up and using Podman.

Start the local infrastructure (Postgres on 5432, Keycloak on 8085):

```
podman-compose up -d
```

During development it is recommended to use the profile `local`. The application has three profiles configured: `dev` (default), `local`, and `prod`. In IntelliJ `-Dspring.profiles.active=local` can be added in the VM options of the Run Configuration after enabling this property in "Modify options". Create your own `application-local.yml` file to override settings for development.


Run the Spring Boot app with the `local` profile from the CLI:

```
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

````
Swagger: http://localhost:8080/swagger-ui/index.html
Actuator Endpoints: http://localhost:8080/actuator
````

The application exposes the following actuator endpoints: info, health, and auditevents. You can access detailed health information at http://localhost:8080/actuator/health.

Lombok must be supported by your IDE. For IntelliJ install the Lombok plugin and enable annotation processing - [learn more](https://bootify.io/next-steps/spring-boot-with-lombok.html).

In addition to the Spring Boot application, the DevServer must also be started - for this [Node.js](https://nodejs.org/) version v22.19.0 is required. The application uses Java 21. On first usage and after updates the dependencies have to be installed:

```
npm install
```

The DevServer can be started as follows:

```
npm run devserver
```

Using a proxy the whole application is now accessible under `http://localhost:3000`. The API is served by Spring Boot on `http://localhost:8080`. All changes to the templates and JS/CSS files are immediately visible in the browser.

## Authentication and login

- Keycloak Admin Console:
    - URL: http://localhost:8085
    - Username: admin
    - Password: P4ssword!

- Application users (seeded in the digi-id realm):
    - `admin@invalid.io` — role: ADMIN — password: `admin`
    - `user@invalid.io` — role: USER — password: `admin`

- If a login fails, reset the user's password in Keycloak:
    - Log in to the Admin Console (above)
    - Users -> select the user -> Credentials -> Set Password
    - Enter a new password and set Temporary to OFF -> Save

Obtain an access token (password grant) and call the API:

```bash
# Get token
curl -X POST \
  http://localhost:8085/realms/digi-id/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=booking-id&grant_type=password&username=admin@invalid.io&password=admin"

# Call API with the token
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" http://localhost:8080/api/v1/bookings
```

Frontend login
- The frontend at `http://localhost:3000` will redirect to Keycloak and accepts the same credentials listed above.

## Testing requirements

Testcontainers is used for running the integration tests. Due to the reuse flag, the container will not shut down after the tests. It can be stopped manually if needed.

The `ModularityTest` verifies the module structure and adds a documentation in `target/spring-modulith-docs`.

Frontend unit tests can be executed with `npm run test`.

## Build

The application can be tested and built using the following command:

```
mvnw clean package
```

Node.js is automatically downloaded using the `frontend-maven-plugin` and the final JS/CSS files are integrated into the jar.

Start your application with the following command - here with the profile `production`:

```
java -Dspring.profiles.active=production -jar ./target/digi-booking-app-0.0.1-SNAPSHOT.jar
```

If required, a container image can be created with the Spring Boot plugin. Add `SPRING_PROFILES_ACTIVE=production` as environment variable when running the container.

```
mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=digi.booking/digi-booking-app
```

Alternatively, you can build an image using Podman:

```
podman build -t digi.booking/digi-booking-app -f Containerfile .
```

## CI/CD Pipeline

This project uses GitHub Actions for continuous integration and deployment. The pipeline is configured in `.github/workflows/ci-cd.yml` and consists of the following stages:

> **Note:** While local development uses Podman, the CI/CD pipeline still uses Docker. This separation allows for local development with Podman's advantages while maintaining compatibility with GitHub Actions' Docker-based infrastructure.

### Build and Test

This stage:
- Sets up a PostgreSQL database for testing
- Builds and tests the frontend (React) application
- Builds and tests the backend (Spring Boot) application
- Uploads test results and coverage reports

### Docker Image Build

When code is pushed to the main branch, this stage:
- Builds a Docker image using the Containerfile
- Pushes the image to Docker Hub with tags for 'latest' and the commit SHA

### Deployment

When code is pushed to the main branch, this stage:
- Deploys the application to the production environment
- This stage is currently a placeholder and needs to be customized based on your deployment infrastructure

### Required Secrets

The following secrets need to be configured in your GitHub repository:
- `DOCKER_HUB_USERNAME`: Your Docker Hub username
- `DOCKER_HUB_TOKEN`: Your Docker Hub access token
- `CODECOV_TOKEN`: Your Codecov token (optional, for code coverage reporting)

### Running the Pipeline Locally

You can test parts of the pipeline locally:

```powershell
# Build and test frontend
npm ci
npm test
npm run build

# Build and test backend
.\mvnw clean verify

# Build container image with Podman
podman build -t digi-booking-app:local -f Containerfile .
```

## Key Technologies

* Java 21
* Spring Boot 3.5.6
* Spring Modulith 1.4.3
* PostgreSQL
* Keycloak for authentication
* React with Node.js v22.19.0
* Testcontainers for integration testing
* Podman for containerization

## Further readings

* [Maven docs](https://maven.apache.org/guides/index.html)
* [Spring Boot reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
* [Spring Data JPA reference](https://docs.spring.io/spring-data/jpa/reference/jpa.html)
* [Spring Modulith](https://spring.io/projects/spring-modulith)
* [Learn React](https://react.dev/learn)
* [Webpack concepts](https://webpack.js.org/concepts/)
* [npm docs](https://docs.npmjs.com/)
* [Tailwind CSS](https://tailwindcss.com/)
* [GitHub Actions documentation](https://docs.github.com/en/actions)
* [Podman documentation](https://docs.podman.io/)
* [Keycloak documentation](https://www.keycloak.org/documentation)
