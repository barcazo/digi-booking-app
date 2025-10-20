package digi.booking.digi_booking_app.base.room;

import digi.booking.digi_booking_app.base.RoomService;
import digi.booking.digi_booking_app.base.events.BeforeDeleteRoom;
import digi.booking.digi_booking_app.base.util.CustomCollectors;
import digi.booking.digi_booking_app.base.util.NotFoundException;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ApplicationEventPublisher publisher;
    private final RoomMapper roomMapper;

    public RoomServiceImpl(final RoomRepository roomRepository,
            final ApplicationEventPublisher publisher, final RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.publisher = publisher;
        this.roomMapper = roomMapper;
    }

    @Override
    public Page<RoomDTO> findAll(final String filter, final Pageable pageable) {
        Page<Room> page;
        if (filter != null) {
            UUID uuidFilter = null;
            try {
                uuidFilter = UUID.fromString(filter);
            } catch (final IllegalArgumentException illegalArgumentException) {
                // keep null - no parseable input
            }
            page = roomRepository.findAllById(uuidFilter, pageable);
        } else {
            page = roomRepository.findAll(pageable);
        }
        return new PageImpl<>(page.getContent()
                .stream()
                .map(room -> roomMapper.updateRoomDTO(room, new RoomDTO()))
                .toList(),
                pageable, page.getTotalElements());
    }

    @Override
    public RoomDTO get(final UUID id) {
        return roomRepository.findById(id)
                .map(room -> roomMapper.updateRoomDTO(room, new RoomDTO()))
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public UUID create(final RoomDTO roomDTO) {
        final Room room = new Room();
        roomMapper.updateRoom(roomDTO, room);
        return roomRepository.save(room).getId();
    }

    @Override
    public void update(final UUID id, final RoomDTO roomDTO) {
        final Room room = roomRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        roomMapper.updateRoom(roomDTO, room);
        roomRepository.save(room);
    }

    @Override
    public void delete(final UUID id) {
        final Room room = roomRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteRoom(id));
        roomRepository.delete(room);
    }

    @Override
    public Map<UUID, String> getRoomValues() {
        return roomRepository.findAll(Sort.by("id"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Room::getId, Room::getRoomType));
    }

}
