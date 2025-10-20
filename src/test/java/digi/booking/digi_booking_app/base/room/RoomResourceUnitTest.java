package digi.booking.digi_booking_app.base.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import digi.booking.digi_booking_app.base.RoomService;
import digi.booking.digi_booking_app.base.model.SimpleValue;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class RoomResourceUnitTest {

    @Mock
    private RoomService roomService;
    @Mock
    private RoomAssembler roomAssembler;
    @Mock
    private PagedResourcesAssembler<RoomDTO> pagedResourcesAssembler;

    private RoomResource roomResource;

    @BeforeEach
    void setUp() {
        roomResource = new RoomResource(roomService, roomAssembler, pagedResourcesAssembler);
    }

    @Test
    void getAllRooms_returnsPagedModel() {
        Pageable pageable = PageRequest.of(0, 20);
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setId(UUID.randomUUID());
        Page<RoomDTO> page = new PageImpl<>(List.of(roomDTO), pageable, 1);
        PagedModel<EntityModel<RoomDTO>> pagedModel = (PagedModel<EntityModel<RoomDTO>>) PagedModel.of(List.of(EntityModel.of(roomDTO)));

        when(roomService.findAll(null, pageable)).thenReturn(page);
        when(pagedResourcesAssembler.toModel(page, roomAssembler)).thenReturn(pagedModel);

        ResponseEntity<PagedModel<EntityModel<RoomDTO>>> response = roomResource.getAllRooms(null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pagedModel);
        verify(roomService).findAll(null, pageable);
        verify(pagedResourcesAssembler).toModel(page, roomAssembler);
    }

    @Test
    void getRoom_returnsEntityModel() {
        UUID id = UUID.randomUUID();
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setId(id);
        EntityModel<RoomDTO> roomModel = EntityModel.of(roomDTO);

        when(roomService.get(id)).thenReturn(roomDTO);
        when(roomAssembler.toModel(roomDTO)).thenReturn(roomModel);

        ResponseEntity<EntityModel<RoomDTO>> response = roomResource.getRoom(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(roomModel);
        verify(roomService).get(id);
        verify(roomAssembler).toModel(roomDTO);
    }

    @Test
    void createRoom_returnsCreated() {
        UUID createdId = UUID.randomUUID();
        RoomDTO roomDTO = new RoomDTO();
        EntityModel<SimpleValue<UUID>> simpleModel = SimpleValue.entityModelOf(createdId);

        when(roomService.create(roomDTO)).thenReturn(createdId);
        when(roomAssembler.toSimpleModel(createdId)).thenReturn(simpleModel);

        ResponseEntity<EntityModel<SimpleValue<UUID>>> response = roomResource.createRoom(roomDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(simpleModel);
        verify(roomService).create(roomDTO);
        verify(roomAssembler).toSimpleModel(createdId);
    }

    @Test
    void updateRoom_returnsOk() {
        UUID id = UUID.randomUUID();
        RoomDTO roomDTO = new RoomDTO();
        EntityModel<SimpleValue<UUID>> simpleModel = SimpleValue.entityModelOf(id);

        when(roomAssembler.toSimpleModel(id)).thenReturn(simpleModel);

        ResponseEntity<EntityModel<SimpleValue<UUID>>> response = roomResource.updateRoom(id, roomDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(simpleModel);
        verify(roomService).update(id, roomDTO);
        verify(roomAssembler).toSimpleModel(id);
    }

    @Test
    void deleteRoom_returnsNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = roomResource.deleteRoom(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(roomService).delete(id);
    }
}