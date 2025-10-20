# Booking Api System

This app was created with Spring Initializer- tips on working with the code [can be found here](https://spring.io/).

## Development

When starting the application `docker compose up` is called and the app will connect to the contained services. [Docker](https://www.docker.com/get-started/) must be available on the current system.

Start the local infrastructure (Postgres on 5433, Keycloak on 8085):

```
docker compose up -d
```

During development it is recommended to use the profile `local`. In IntelliJ `-Dspring.profiles.active=local` can be added in the VM options of the Run Configuration after enabling this property in "Modify options". Create your own `application-local.yml` file to override settings for development.


Run the Spring Boot app with the `local` profile from the CLI:

```
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

````
Swagger: http://localhost:8080/swagger-ui/index.html
````

Lombok must be supported by your IDE. For IntelliJ install the Lombok plugin and enable annotation processing - [learn more](https://bootify.io/next-steps/spring-boot-with-lombok.html).

In addition to the Spring Boot application, the DevServer must also be started - for this [Node.js](https://nodejs.org/) version 22 is required. On first usage and after updates the dependencies have to be installed:

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

- Application users (seeded in the booking realm):
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
  http://localhost:8085/realms/booking/protocol/openid-connect/token \
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
java -Dspring.profiles.active=production -jar ./target/booking-api-system-0.0.1-SNAPSHOT.jar
```

If required, a Docker image can be created with the Spring Boot plugin. Add `SPRING_PROFILES_ACTIVE=production` as environment variable when running the container.

```
mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=digi.booking/booking-api-system
```

## Further readings

* [Maven docs](https://maven.apache.org/guides/index.html)
* [Spring Boot reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
* [Spring Data JPA reference](https://docs.spring.io/spring-data/jpa/reference/jpa.html)
* [Learn React](https://react.dev/learn)
* [Webpack concepts](https://webpack.js.org/concepts/)
* [npm docs](https://docs.npmjs.com/)
* [Tailwind CSS](https://tailwindcss.com/)  
