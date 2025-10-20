package digi.booking.digi_booking_app.base.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import digi.booking.digi_booking_app.base.events.BeforeDeleteRoom;
import digi.booking.digi_booking_app.base.exception.RoomNotAvailableException;
import digi.booking.digi_booking_app.base.room.Room;
import digi.booking.digi_booking_app.base.room.RoomRepository;
import digi.booking.digi_booking_app.base.user.User;
import digi.booking.digi_booking_app.base.user.UserRepository;
import digi.booking.digi_booking_app.base.util.NotFoundException;
import digi.booking.digi_booking_app.base.util.ReferencedException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Booking booking;
    private BookingDTO bookingDTO;
    private Room room;
    private User user;

    @BeforeEach
    void setUp() {
        room = new Room();
        room.setId(UUID.randomUUID());
        room.setRoomNumber(42);
        room.setActive(true);

        user = new User();
        user.setKeycloakId(100L);
        user.setActive(true);

        booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setRoom(room);
        booking.setUser(user);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setCheckinDate(LocalDate.now());
        booking.setCheckoutDate(LocalDate.now().plusDays(2));

        bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setRoom(room.getId());
        bookingDTO.setUser(user.getKeycloakId());
        bookingDTO.setStatus(BookingStatus.ACTIVE);
        bookingDTO.setCheckinDate(booking.getCheckinDate());
        bookingDTO.setCheckoutDate(booking.getCheckoutDate());
    }

    @Test
    void findAll_returnsPageOfBookings() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Booking> page = new PageImpl<>(List.of(booking), pageable, 1);
        BookingDTO mappedDto = new BookingDTO();
        when(bookingRepository.findAll(pageable)).thenReturn(page);
        when(bookingMapper.updateBookingDTO(eq(booking), any(BookingDTO.class))).thenReturn(mappedDto);

        Page<BookingDTO> result = bookingService.findAll(null, pageable);

        assertThat(result.getContent()).containsExactly(mappedDto);
        verify(bookingRepository).findAll(pageable);
        verify(bookingMapper).updateBookingDTO(eq(booking), any(BookingDTO.class));
    }

    @Test
    void findAll_withFilter_usesRepositoryById() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Booking> page = new PageImpl<>(List.of(booking), pageable, 1);
        BookingDTO mappedDto = new BookingDTO();
        when(bookingRepository.findAllById(booking.getId(), pageable)).thenReturn(page);
        when(bookingMapper.updateBookingDTO(eq(booking), any(BookingDTO.class))).thenReturn(mappedDto);

        Page<BookingDTO> result = bookingService.findAll(booking.getId().toString(), pageable);

        assertThat(result.getContent()).containsExactly(mappedDto);
        verify(bookingRepository).findAllById(booking.getId(), pageable);
    }


    @Test
    void get_returnsBooking() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingMapper.updateBookingDTO(eq(booking), any(BookingDTO.class))).thenReturn(bookingDTO);

        BookingDTO result = bookingService.get(booking.getId());

        assertThat(result).isEqualTo(bookingDTO);
        verify(bookingRepository).findById(booking.getId());
        verify(bookingMapper).updateBookingDTO(eq(booking), any(BookingDTO.class));
    }

    @Test
    void get_notFound_throwsException() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.get(booking.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_persistsBookingWhenValid() {
        when(userRepository.findById(user.getKeycloakId())).thenReturn(Optional.of(user));
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(roomRepository.findByRoomNumber(room.getRoomNumber())).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingActiveBookings(room, bookingDTO.getCheckinDate(), bookingDTO.getCheckoutDate(), BookingStatus.ACTIVE, null))
                .thenReturn(Collections.emptyList());
        Booking savedBooking = new Booking();
        savedBooking.setId(UUID.randomUUID());
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        UUID result = bookingService.create(bookingDTO);

        assertThat(result).isEqualTo(savedBooking.getId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void create_roomInactive_throwsRoomNotAvailableException() {
        room.setActive(false);
        when(userRepository.findById(user.getKeycloakId())).thenReturn(Optional.of(user));
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> bookingService.create(bookingDTO))
                .isInstanceOf(RoomNotAvailableException.class);
    }

    @Test
    void create_overlappingBooking_throwsException() {
        when(userRepository.findById(user.getKeycloakId())).thenReturn(Optional.of(user));
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(roomRepository.findByRoomNumber(room.getRoomNumber())).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingActiveBookings(room, bookingDTO.getCheckinDate(), bookingDTO.getCheckoutDate(), BookingStatus.ACTIVE, null))
                .thenReturn(List.of(booking));

        assertThatThrownBy(() -> bookingService.create(bookingDTO))
                .isInstanceOf(RoomNotAvailableException.class);
    }

    @Test
    void update_existingBooking_succeeds() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(userRepository.findById(user.getKeycloakId())).thenReturn(Optional.of(user));
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(roomRepository.findByRoomNumber(room.getRoomNumber())).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingActiveBookings(room, bookingDTO.getCheckinDate(), bookingDTO.getCheckoutDate(), BookingStatus.ACTIVE, booking.getId()))
                .thenReturn(Collections.emptyList());

        bookingService.update(booking.getId(), bookingDTO);

        verify(bookingRepository).findById(booking.getId());
        verify(bookingRepository).save(booking);
    }

    @Test
    void update_notFound_throwsException() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.update(booking.getId(), bookingDTO))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void cancel_updatesStatusToCancelled() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        bookingService.cancel(booking.getId());

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancel_notFound_throwsException() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancel(booking.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_removesBooking() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        bookingService.delete(booking.getId());

        verify(bookingRepository).delete(booking);
    }

    @Test
    void delete_notFound_throwsException() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.delete(booking.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void onBeforeDeleteRoom_bookingPresent_throwsReferencedException() {
        when(bookingRepository.findFirstByRoomId(room.getId())).thenReturn(booking);

        assertThatThrownBy(() -> bookingService.on(new BeforeDeleteRoom(room.getId())))
                .isInstanceOf(ReferencedException.class);
    }

    @Test
    void onBeforeDeleteRoom_noBooking_noException() {
        when(bookingRepository.findFirstByRoomId(room.getId())).thenReturn(null);

        bookingService.on(new BeforeDeleteRoom(room.getId()));

        verify(bookingRepository).findFirstByRoomId(room.getId());
    }
}