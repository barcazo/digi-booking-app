package digi.booking.digi_booking_app.base.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import digi.booking.digi_booking_app.DigiBookingAppApplication;
import digi.booking.digi_booking_app.base.config.BaseIT;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.jdbc.Sql;


@ApplicationModuleTest(
        classes = DigiBookingAppApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        mode = ApplicationModuleTest.BootstrapMode.ALL_DEPENDENCIES
)
public class BookingResourceTest extends BaseIT {

    @Autowired
    public BookingRepository bookingRepository;

    @Test
    @Sql({"/data/roomData.sql", "/data/userData.sql", "/data/bookingData.sql"})
    void getAllBookings_success() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, bookingApiSecurityToken(ADMIN))
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/v1/bookings")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("page.totalElements", Matchers.equalTo(2))
                    .body("_embedded.bookingDTOList.get(0).id", Matchers.equalTo("a9b7ba70-783b-317e-9998-dc4dd82eb3c5"))
                    .body("_links.self.href", Matchers.endsWith("/api/v1/bookings?page=0&size=20&sort=id,asc"));
    }

    @Test
    @Sql({"/data/roomData.sql", "/data/userData.sql", "/data/bookingData.sql"})
    void getAllBookings_filtered() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, bookingApiSecurityToken(ADMIN))
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/v1/bookings?filter=b8c37e33-defd-351c-b91e-1e03e51657da")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("page.totalElements", Matchers.equalTo(1))
                    .body("_embedded.bookingDTOList.get(0).id", Matchers.equalTo("b8c37e33-defd-351c-b91e-1e03e51657da"));
    }

    @Test
    void getAllBookings_unauthorized() {
        RestAssured
                .given()
                    .redirects().follow(false)
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/v1/bookings")
                .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .body("code", Matchers.equalTo("AUTHORIZATION_DENIED"));
    }

    @Test
    @Sql({"/data/roomData.sql", "/data/userData.sql", "/data/bookingData.sql"})
    void getBooking_success() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, bookingApiSecurityToken(ADMIN))
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/v1/bookings/a9b7ba70-783b-317e-9998-dc4dd82eb3c5")
                .then()
                    .statusCode(HttpStatus.OK.value());
    }

    @Test
    void getBooking_notFound() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, bookingApiSecurityToken(ADMIN))
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/v1/bookings/23d7c8a0-8b4a-3a1b-87c5-99473f5dddda")
                .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", Matchers.equalTo("NOT_FOUND"));
    }

    @Test
    @Sql({"/data/roomData.sql", "/data/userData.sql"})
    void createBooking_success() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, bookingApiSecurityToken(ADMIN))
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/bookingDTORequest.json"))
                .when()
                    .post("/api/v1/bookings")
                .then()
                    .statusCode(HttpStatus.CREATED.value());
        assertEquals(1, bookingRepository.count());
    }

    @Test
    void createBooking_missingField() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, bookingApiSecurityToken(ADMIN))
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/bookingDTORequest_missingField.json"))
                .when()
                    .post("/api/v1/bookings")
                .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", Matchers.equalTo("VALIDATION_FAILED"))
                    .body("fieldErrors.get(0).property", Matchers.equalTo("checkinDate"))
                    .body("fieldErrors.get(0).code", Matchers.equalTo("REQUIRED_NOT_NULL"));
    }

    @Test
    @Sql({"/data/roomData.sql", "/data/userData.sql", "/data/bookingData.sql"})
    void updateBooking_success() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, bookingApiSecurityToken(ADMIN))
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/bookingDTORequest.json"))
                .when()
                    .put("/api/v1/bookings/a9b7ba70-783b-317e-9998-dc4dd82eb3c5")
                .then()
                    .statusCode(HttpStatus.OK.value());
        assertEquals(2, bookingRepository.count());
    }

    @Test
    @Sql({"/data/roomData.sql", "/data/userData.sql", "/data/bookingData.sql"})
    void deleteBooking_success() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, bookingApiSecurityToken(ADMIN))
                    .accept(ContentType.JSON)
                .when()
                    .delete("/api/v1/bookings/a9b7ba70-783b-317e-9998-dc4dd82eb3c5")
                .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        assertEquals(1, bookingRepository.count());
    }

}
