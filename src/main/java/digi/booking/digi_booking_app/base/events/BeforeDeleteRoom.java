package digi.booking.digi_booking_app.base.events;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class BeforeDeleteRoom {

    private UUID id;

}
