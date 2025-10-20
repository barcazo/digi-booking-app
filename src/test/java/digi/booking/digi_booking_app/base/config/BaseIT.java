package digi.booking.digi_booking_app.base.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.PostgreSQLContainer;


/**
 * Abstract base class to be extended by every IT test. Starts the Spring Boot context with a
 * Datasource connected to the Testcontainers Docker instance. The instance is reused for all tests,
 * with all data wiped out before each test.
 */
@ActiveProfiles("it")
@Sql("/data/clearAll.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public abstract class BaseIT {

    @ServiceConnection
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.6");
    private static final KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:26.0.6");
    public static final String ADMIN = "admin@invalid.io";
    public static final String USER = "user@invalid.io";
    public static final String PASSWORD = "Bootify!";

    static {
        postgreSQLContainer.withReuse(true)
                .start();
        keycloakContainer.withRealmImportFile("keycloak-realm.json")
                .withReuse(true)
                .start();
    }

    @LocalServerPort
    public int serverPort;

    private final HashMap<String, String> bookingApiSecurityTokens = new HashMap<>();

    @PostConstruct
    public void initRestAssured() {
        RestAssured.port = serverPort;
        RestAssured.urlEncodingEnabled = false;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @DynamicPropertySource
    public static void setDynamicProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> keycloakContainer.getAuthServerUrl() + "/realms/digi-id/protocol/openid-connect/certs");
    }

    @SneakyThrows
    public String readResource(final String resourceName) {
        return StreamUtils.copyToString(getClass().getResourceAsStream(resourceName), StandardCharsets.UTF_8);
    }

    public String bookingApiSecurityToken(final String username) {
        String bookingApiSecurityToken = bookingApiSecurityTokens.get(username);
        if (bookingApiSecurityToken == null) {
            // get a fresh token
            final String tokenUrl = keycloakContainer.getAuthServerUrl() + "/realms/digi-id/protocol/openid-connect/token";
            final Map<String, Object> keycloakTokenResponse = RestAssured
                    .given()
                        .accept(ContentType.JSON)
                        .contentType(ContentType.URLENC)
                        .formParam("grant_type", "password")
                        .formParam("client_id", "digi-id")
                        .formParam("client_secret", "F1CEA53BFE07501CB4DDB413812D0210")
                        .formParam("username", username)
                        .formParam("password", PASSWORD)
                    .when()
                        .post(tokenUrl)
                    .body().as(new TypeRef<>() {
                    });
            bookingApiSecurityToken = "Bearer " + keycloakTokenResponse.get("access_token");
            bookingApiSecurityTokens.put(username, bookingApiSecurityToken);
        }
        return bookingApiSecurityToken;
    }

}
