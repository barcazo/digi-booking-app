package digi.booking.digi_booking_app.base.room;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoomRepository extends JpaRepository<Room, UUID> {

    Page<Room> findAllById(UUID id, Pageable pageable);

    Optional<Room> findByRoomNumber(Integer roomNum);
}
