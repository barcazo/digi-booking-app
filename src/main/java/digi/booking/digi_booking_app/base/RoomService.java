package digi.booking.digi_booking_app.base;

import digi.booking.digi_booking_app.base.room.RoomDTO;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface RoomService {

    Page<RoomDTO> findAll(String filter, Pageable pageable);

    RoomDTO get(UUID id);

    UUID create(RoomDTO roomDTO);

    void update(UUID id, RoomDTO roomDTO);

    void delete(UUID id);

    Map<UUID, String> getRoomValues();

}
