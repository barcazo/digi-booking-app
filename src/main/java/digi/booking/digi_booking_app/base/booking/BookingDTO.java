package digi.booking.digi_booking_app.base.booking;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class BookingDTO {

    private UUID id;

    @NotNull
    private LocalDate checkinDate;

    @NotNull
    private LocalDate checkoutDate;

    @NotNull
    private BookingStatus status;

    @NotNull
    private Long user;

    @NotNull
    private UUID room;

}
