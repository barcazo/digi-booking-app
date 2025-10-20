package digi.booking.digi_booking_app.base.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RoomNotAvailableException extends RuntimeException {

    public RoomNotAvailableException() {
        super();
    }

    public RoomNotAvailableException(final String message) {
        super(message);
    }

}
