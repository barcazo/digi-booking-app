package digi.booking.digi_booking_app.base.booking;

import digi.booking.digi_booking_app.base.room.Room;
import digi.booking.digi_booking_app.base.room.RoomRepository;
import digi.booking.digi_booking_app.base.user.User;
import digi.booking.digi_booking_app.base.user.UserRepository;
import digi.booking.digi_booking_app.base.util.NotFoundException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;


@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BookingMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "room", ignore = true)
    BookingDTO updateBookingDTO(Booking booking, @MappingTarget BookingDTO bookingDTO);

    @AfterMapping
    default void afterUpdateBookingDTO(Booking booking, @MappingTarget BookingDTO bookingDTO) {
        bookingDTO.setUser(booking.getUser() == null ? null : booking.getUser().getKeycloakId());
        bookingDTO.setRoom(booking.getRoom() == null ? null : booking.getRoom().getId());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "room", ignore = true)
    Booking updateBooking(BookingDTO bookingDTO, @MappingTarget Booking booking,
            @Context UserRepository userRepository, @Context RoomRepository roomRepository);

    @AfterMapping
    default void afterUpdateBooking(BookingDTO bookingDTO, @MappingTarget Booking booking,
            @Context UserRepository userRepository, @Context RoomRepository roomRepository) {
        final User user = bookingDTO.getUser() == null ? null : userRepository.findById(bookingDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
        booking.setUser(user);
        final Room room = bookingDTO.getRoom() == null ? null : roomRepository.findById(bookingDTO.getRoom())
                .orElseThrow(() -> new NotFoundException("room not found"));
        booking.setRoom(room);
    }

}
