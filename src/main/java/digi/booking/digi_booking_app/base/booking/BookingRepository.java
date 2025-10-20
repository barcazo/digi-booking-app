package digi.booking.digi_booking_app.base.booking;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import digi.booking.digi_booking_app.base.room.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Page<Booking> findAllById(UUID id, Pageable pageable);

    Booking findFirstByRoomId(UUID id);

    /**
     * Find overlapping active bookings for a room.
     *
     * @param room the room to check
     * @param checkin the check-in date
     * @param checkout the check-out date
     * @param status the booking status
     * @param excludeId the booking ID to exclude from the search
     * @return a list of overlapping active bookings
     */
    @Query("SELECT b FROM Booking b WHERE b.room = :room AND b.status = :status AND (b.checkinDate < :checkout AND b.checkoutDate > :checkin) AND (:excludeId IS NULL OR b.id <> :excludeId)")
    List<Booking> findOverlappingActiveBookings(
            @Param("room") Room room,
            @Param("checkin") LocalDate checkin,
            @Param("checkout") LocalDate checkout,
            @Param("status") BookingStatus status,
            @Param("excludeId") UUID excludeId
    );

}
