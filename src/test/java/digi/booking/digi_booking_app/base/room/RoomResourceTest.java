package digi.booking.digi_booking_app.base.room;

import static org.junit.jupiter.api.Assertions.assertEquals;

import digi.booking.digi_booking_app.DigiBookingAppApplication;
import digi.booking.digi_booking_app.base.config.BaseIT;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.UUID;
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
public class RoomResourceTest extends BaseIT {

    @Autowired
    public RoomRepository roomRepository;

    @Test
    @Sql("/data/roomData.sql")
    void getAllRooms_success() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, bookingApiSecurityToken(USER))
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/v1/rooms")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("page.totalElements", Matchers.equalTo(2))
                    .body("_embedded.roomDTOList.get(0).id", Matchers.equalTo("a92d0103-08a6-3379-9a3d-9c728ee74244"))
                    .body("_links.self.href", Matchers.endsWith("/api/v1/rooms?page=0&size=20&sort=id,asc"));
    }

    @Test
    @Sql("/data/roomData.sql")
    void getAllRooms_filtered() {
        RestAssured
                .given()
                    .header(HttpHeaders.AUTHORIZATION, bookingApiSecurityToken(USER))
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/v1/rooms?filter=b801e5d4-da87-3c39-9782-741cd794002d")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("page.totalElements", Matchers.equalTo(1))
                    .body("_embedded.roomDTOList.get(0).id", Matchers.equalTo("b801e5d4-da87-3c39-9782-741cd794002d"));
    }

    @Test
    @Sql("/data/roomData.sql")
    void getRoom_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/v1/rooms/a92d0103-08a6-3379-9a3d-9c728ee74244")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("roomNumber", Matchers.equalTo(92))
                    .body("_links.self.href", Matchers.endsWith("/api/v1/rooms/a92d0103-08a6-3379-9a3d-9c728ee74244"));
    }

    @Test
    void getRoom_notFound() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .get("/api/v1/rooms/23de10ad-baa1-32ee-93f7-7f679fa1483a")
                .then()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", Matchers.equalTo("NOT_FOUND"));
    }

    @Test
    void createRoom_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/roomDTORequest.json"))
                .when()
                    .post("/api/v1/rooms")
                .then()
                    .statusCode(HttpStatus.CREATED.value());
        assertEquals(1, roomRepository.count());
    }

    @Test
    void createRoom_missingField() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/roomDTORequest_missingField.json"))
                .when()
                    .post("/api/v1/rooms")
                .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", Matchers.equalTo("VALIDATION_FAILED"))
                    .body("fieldErrors.get(0).property", Matchers.equalTo("roomNumber"))
                    .body("fieldErrors.get(0).code", Matchers.equalTo("REQUIRED_NOT_NULL"));
    }

    @Test
    @Sql("/data/roomData.sql")
    void updateRoom_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .body(readResource("/requests/roomDTORequest.json"))
                .when()
                    .put("/api/v1/rooms/a92d0103-08a6-3379-9a3d-9c728ee74244")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("_links.self.href", Matchers.endsWith("/api/v1/rooms/a92d0103-08a6-3379-9a3d-9c728ee74244"));
        assertEquals(77, roomRepository.findById(UUID.fromString("a92d0103-08a6-3379-9a3d-9c728ee74244")).orElseThrow().getRoomNumber());
        assertEquals(2, roomRepository.count());
    }

    @Test
    @Sql("/data/roomData.sql")
    void deleteRoom_success() {
        RestAssured
                .given()
                    .accept(ContentType.JSON)
                .when()
                    .delete("/api/v1/rooms/a92d0103-08a6-3379-9a3d-9c728ee74244")
                .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        assertEquals(1, roomRepository.count());
    }

}
