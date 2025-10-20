package digi.booking.digi_booking_app.base.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import digi.booking.digi_booking_app.base.events.BeforeDeleteRoom;
import digi.booking.digi_booking_app.base.util.CustomCollectors;
import digi.booking.digi_booking_app.base.util.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Room room;
    private RoomDTO roomDTO;

    @BeforeEach
    void setUp() {
        room = new Room();
        room.setId(UUID.randomUUID());
        room.setRoomNumber(101);
        room.setRoomType("Deluxe");
        room.setCapacity(2);
        room.setPrice(new BigDecimal("100.00"));
        room.setAmenities("WiFi");
        room.setActive(true);

        roomDTO = new RoomDTO();
        roomDTO.setId(room.getId());
        roomDTO.setRoomNumber(room.getRoomNumber());
        roomDTO.setRoomType(room.getRoomType());
        roomDTO.setCapacity(room.getCapacity());
        roomDTO.setPrice(room.getPrice());
        roomDTO.setAmenities(room.getAmenities());
        roomDTO.setActive(room.getActive());
    }

    @Test
    void findAll_returnsAllRooms() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Room> page = new PageImpl<>(List.of(room), pageable, 1);
        RoomDTO mappedDto = new RoomDTO();
        when(roomRepository.findAll(pageable)).thenReturn(page);
        when(roomMapper.updateRoomDTO(eq(room), any(RoomDTO.class))).thenReturn(mappedDto);

        Page<RoomDTO> result = roomService.findAll(null, pageable);

        assertThat(result.getContent()).containsExactly(mappedDto);
        verify(roomRepository).findAll(pageable);
        verify(roomMapper).updateRoomDTO(eq(room), any(RoomDTO.class));
    }

    @Test
    void findAll_withFilter_parsesUuidAndDelegates() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Room> page = new PageImpl<>(List.of(room), pageable, 1);
        RoomDTO mappedDto = new RoomDTO();
        when(roomRepository.findAllById(room.getId(), pageable)).thenReturn(page);
        when(roomMapper.updateRoomDTO(eq(room), any(RoomDTO.class))).thenReturn(mappedDto);

        Page<RoomDTO> result = roomService.findAll(room.getId().toString(), pageable);

        assertThat(result.getContent()).containsExactly(mappedDto);
        verify(roomRepository).findAllById(room.getId(), pageable);
    }

    @Test
    void findAll_withInvalidFilter_ignoresFilter() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Room> page = new PageImpl<>(List.of(room), pageable, 1);
        RoomDTO mappedDto = new RoomDTO();
        when(roomRepository.findAll(pageable)).thenReturn(page);
        when(roomMapper.updateRoomDTO(eq(room), any(RoomDTO.class))).thenReturn(mappedDto);

        Page<RoomDTO> result = roomService.findAll("invalid", pageable);

        assertThat(result.getContent()).containsExactly(mappedDto);
        verify(roomRepository).findAll(pageable);
        verify(roomRepository, never()).findAllById(any(), eq(pageable));
    }

    @Test
    void get_returnsRoom() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(roomMapper.updateRoomDTO(eq(room), any(RoomDTO.class))).thenReturn(roomDTO);

        RoomDTO result = roomService.get(room.getId());

        assertThat(result).isEqualTo(roomDTO);
        verify(roomRepository).findById(room.getId());
        verify(roomMapper).updateRoomDTO(eq(room), any(RoomDTO.class));
    }

    @Test
    void get_notFound_throwsException() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.get(room.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_savesRoom() {
        Room savedRoom = new Room();
        savedRoom.setId(UUID.randomUUID());
        when(roomMapper.updateRoom(roomDTO, room)).thenReturn(room);
        when(roomRepository.save(room)).thenReturn(savedRoom);

        UUID result = roomService.create(roomDTO);

        assertThat(result).isEqualTo(savedRoom.getId());
        verify(roomMapper).updateRoom(roomDTO, room);
        verify(roomRepository).save(room);
    }

    @Test
    void update_existingRoom_savesUpdatedRoom() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(roomMapper.updateRoom(roomDTO, room)).thenReturn(room);

        roomService.update(room.getId(), roomDTO);

        verify(roomRepository).findById(room.getId());
        verify(roomMapper).updateRoom(roomDTO, room);
        verify(roomRepository).save(room);
    }

    @Test
    void update_notFound_throwsException() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.update(room.getId(), roomDTO))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_existingRoom_publishesEventAndDeletes() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        doNothing().when(publisher).publishEvent(any(BeforeDeleteRoom.class));

        roomService.delete(room.getId());

        verify(roomRepository).findById(room.getId());
        verify(publisher).publishEvent(any(BeforeDeleteRoom.class));
        verify(roomRepository).delete(room);
    }

    @Test
    void delete_notFound_throwsException() {
        when(roomRepository.findById(room.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.delete(room.getId()))
                .isInstanceOf(NotFoundException.class);
        verify(publisher, never()).publishEvent(any());
        verify(roomRepository, never()).delete(any());
    }

    @Test
    void getRoomValues_returnsSortedMap() {
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomRepository.findAll(org.springframework.data.domain.Sort.by("id"))).thenReturn(List.of(room));
        Map<UUID, String> expected = Map.of(room.getId(), room.getRoomType());
        when(roomRepository.findAll(org.springframework.data.domain.Sort.by("id"))).thenReturn(List.of(room));

        Map<UUID, String> result = roomService.getRoomValues();

        assertThat(result).containsExactlyEntriesOf(expected);
        verify(roomRepository, times(1)).findAll(org.springframework.data.domain.Sort.by("id"));
    }
}