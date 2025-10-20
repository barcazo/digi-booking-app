package digi.booking.digi_booking_app.base.booking;

import digi.booking.digi_booking_app.base.BookingService;
import digi.booking.digi_booking_app.base.events.BeforeDeleteRoom;
import digi.booking.digi_booking_app.base.exception.RoomNotAvailableException;
import digi.booking.digi_booking_app.base.room.Room;
import digi.booking.digi_booking_app.base.room.RoomDTO;
import digi.booking.digi_booking_app.base.room.RoomRepository;
import digi.booking.digi_booking_app.base.user.User;
import digi.booking.digi_booking_app.base.user.UserRepository;
import digi.booking.digi_booking_app.base.util.NotFoundException;
import digi.booking.digi_booking_app.base.util.ReferencedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final BookingMapper bookingMapper;
    private final Lock bookingLock = new ReentrantLock();

    public BookingServiceImpl(final BookingRepository bookingRepository,
            final UserRepository userRepository, final RoomRepository roomRepository,
            final BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.bookingMapper = bookingMapper;
    }

    @Override
    public Page<BookingDTO> findAll(final String filter, final Pageable pageable) {
        Page<Booking> page;
        if (filter != null) {
            UUID uuidFilter = null;
            try {
                uuidFilter = UUID.fromString(filter);
            } catch (final IllegalArgumentException illegalArgumentException) {
                // keep null - no parseable input
            }
            page = bookingRepository.findAllById(uuidFilter, pageable);
        } else {
            page = bookingRepository.findAll(pageable);
        }
        return new PageImpl<>(page.getContent()
                .stream()
                .map(booking -> bookingMapper.updateBookingDTO(booking, new BookingDTO()))
                .toList(),
                pageable, page.getTotalElements());
    }

    @Override
    public BookingDTO get(final UUID id) {
        return bookingRepository.findById(id)
                .map(booking -> bookingMapper.updateBookingDTO(booking, new BookingDTO()))
                .orElseThrow(NotFoundException::new);
    }
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UUID create(final BookingDTO bookingDTO) {
        log.info("Attempting to book room {} for dates {} to {}",
                bookingDTO.getRoom(), bookingDTO.getCheckinDate(), bookingDTO.getCheckoutDate());

        final Booking booking = new Booking();

        // Resolve User entity from userId
        User user = userRepository.findById(bookingDTO.getUser())
                .orElseThrow(() -> new NotFoundException("User not found: " + bookingDTO.getUser()));
        booking.setUser(user);

        // Resolve Room entity from room UUID
        Optional<Room> roomOpt = roomRepository.findById(bookingDTO.getRoom());
        Room room = roomOpt.orElseThrow(() -> new NotFoundException("Room not found: " + bookingDTO.getRoom()));
        booking.setRoom(room);

        // Map other fields from DTO
        booking.setCheckinDate(bookingDTO.getCheckinDate());
        booking.setCheckoutDate(bookingDTO.getCheckoutDate());
        booking.setStatus(bookingDTO.getStatus());

        bookingLock.lock();
        try {
            validateRoomAndUser(booking);
            validateDates(booking.getCheckinDate(), booking.getCheckoutDate());
            ensureNoOverlap(room.getRoomNumber(), booking.getCheckinDate(), booking.getCheckoutDate(), null);
            return bookingRepository.save(booking).getId();
        } finally {
            bookingLock.unlock();
        }
    }



    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void update(final UUID id, final BookingDTO bookingDTO) {
        log.info("Attempting to update booking {} for room {} and dates {} to {}",
                id, bookingDTO.getRoom(), bookingDTO.getCheckinDate(), bookingDTO.getCheckoutDate());

        final Booking booking = bookingRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        // Resolve User entity from userId
        User user = userRepository.findById(bookingDTO.getUser())
                .orElseThrow(() -> new NotFoundException("User not found: " + bookingDTO.getUser()));
        booking.setUser(user);

        // Resolve Room entity from room UUID
        Optional<Room> roomOpt = roomRepository.findById(bookingDTO.getRoom());
        Room room = roomOpt.orElseThrow(() -> new NotFoundException("Room not found: " + bookingDTO.getRoom()));
        booking.setRoom(room);

        // Update other fields from DTO
        booking.setCheckinDate(bookingDTO.getCheckinDate());
        booking.setCheckoutDate(bookingDTO.getCheckoutDate());
        booking.setStatus(bookingDTO.getStatus());

        bookingLock.lock();
        try {
            validateRoomAndUser(booking);
            validateDates(booking.getCheckinDate(), booking.getCheckoutDate());
            ensureNoOverlap(room.getRoomNumber(), booking.getCheckinDate(), booking.getCheckoutDate(), booking.getId());
            bookingRepository.save(booking);
        } finally {
            bookingLock.unlock();
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void cancel(final UUID id) {
        log.info("Attempting to cancel booking {}", id);

        final Booking booking = bookingRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        bookingLock.lock();
        try {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        } finally {
            bookingLock.unlock();
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(final UUID id) {
        log.info("Attempting to delete booking {}", id);

        final Booking booking = bookingRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        bookingLock.lock();
        try {
            bookingRepository.delete(booking);
        } finally {
            bookingLock.unlock();
        }
    }

    private void validateRoomAndUser(Booking booking) {
        // Validate Room if present in catalog
        roomRepository.findById(booking.getRoom().getId())
                .ifPresentOrElse(room -> {
                    if (!room.getActive()) {
                        throw new RoomNotAvailableException("Room " + room.getRoomNumber() + " is inactive");
                    }
                }, () -> log.debug("Room {} not managed in catalog; skipping active check", booking.getRoom().getRoomNumber()));

        // Validate User if present in catalog
        userRepository.findById(booking.getUser().getKeycloakId())
                .ifPresent(user -> {
                    if (!user.getActive()) {
                        throw new IllegalStateException("User is inactive");
                    }
                });
    }

    private void validateDates(LocalDate checkin, LocalDate checkout) {
        if (checkin == null || checkout == null) {
            throw new IllegalArgumentException("Both dates required");
        }
        if (checkout.isBefore(checkin)) {
            throw new IllegalArgumentException("Checkout must be on or after checkin");
        }
    }

    private void ensureNoOverlap(Integer roomNum, LocalDate checkin, LocalDate checkout, UUID excludeId) {
        log.info("Checking for overlapping bookings: room={}, checkin={}, checkout={}, excludeId={}",
                roomNum, checkin, checkout, excludeId);

        Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNum);

        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();

            List<Booking> conflicts = bookingRepository.findOverlappingActiveBookings(
                    room, checkin, checkout, BookingStatus.ACTIVE, excludeId);

            log.info("Found {} conflicting bookings", conflicts.size());

            if (!conflicts.isEmpty()) {
                throw new RoomNotAvailableException("Room " + roomNum + " is not available for the selected dates");
            }
        } else {
            log.debug("Room {} not managed in catalog; skipping active check", roomNum);
        }
    }


    @EventListener(BeforeDeleteRoom.class)
    public void on(final BeforeDeleteRoom event) {
        final ReferencedException referencedException = new ReferencedException();
        final Booking roomBooking = bookingRepository.findFirstByRoomId(event.getId());
        if (roomBooking != null) {
            referencedException.setKey("room.booking.room.referenced");
            referencedException.addParam(roomBooking.getId());
            throw referencedException;
        }
    }

}
