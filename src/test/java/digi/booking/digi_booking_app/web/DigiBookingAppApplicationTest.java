package digi.booking.digi_booking_app.web;

import digi.booking.digi_booking_app.DigiBookingAppApplication;
import digi.booking.digi_booking_app.base.config.BaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(
        classes = DigiBookingAppApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class DigiBookingAppApplicationTest extends BaseIT {

    @Test
    void contextLoads() {
    }

}
