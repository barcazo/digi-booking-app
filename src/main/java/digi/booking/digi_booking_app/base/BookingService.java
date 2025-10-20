package digi.booking.digi_booking_app.base;

import digi.booking.digi_booking_app.base.booking.BookingDTO;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface BookingService {

    Page<BookingDTO> findAll(String filter, Pageable pageable);

    BookingDTO get(UUID id);

    UUID create(BookingDTO bookingDTO);

    void update(UUID id, BookingDTO bookingDTO);

    void cancel(UUID id);

    void delete(UUID id);

}
