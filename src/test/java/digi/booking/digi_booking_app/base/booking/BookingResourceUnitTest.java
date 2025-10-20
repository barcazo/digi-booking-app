package digi.booking.digi_booking_app.base.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import digi.booking.digi_booking_app.base.BookingService;
import digi.booking.digi_booking_app.base.RoomService;
import digi.booking.digi_booking_app.base.UserService;
import digi.booking.digi_booking_app.base.model.SimpleValue;
import java.util.List;
import java.util.Map;
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
class BookingResourceUnitTest {

    @Mock
    private BookingService bookingService;
    @Mock
    private BookingAssembler bookingAssembler;
    @Mock
    private PagedResourcesAssembler<BookingDTO> pagedResourcesAssembler;
    @Mock
    private UserService userService;
    @Mock
    private RoomService roomService;

    private BookingResource bookingResource;

    @BeforeEach
    void setUp() {
        bookingResource = new BookingResource(
                bookingService,
                bookingAssembler,
                pagedResourcesAssembler,
                userService,
                roomService
        );
    }

    @Test
    void getBooking_returnsEntityModel() {
        UUID id = UUID.randomUUID();
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(id);
        EntityModel<BookingDTO> model = EntityModel.of(bookingDTO);

        when(bookingService.get(id)).thenReturn(bookingDTO);
        when(bookingAssembler.toModel(bookingDTO)).thenReturn(model);

        ResponseEntity<EntityModel<BookingDTO>> response = bookingResource.getBooking(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(model);
        verify(bookingService).get(id);
        verify(bookingAssembler).toModel(bookingDTO);
    }

    @Test
    void createBooking_returnsCreated() {
        UUID createdId = UUID.randomUUID();
        BookingDTO bookingDTO = new BookingDTO();
        EntityModel<SimpleValue<UUID>> simpleModel = SimpleValue.entityModelOf(createdId);

        when(bookingService.create(bookingDTO)).thenReturn(createdId);
        when(bookingAssembler.toSimpleModel(createdId)).thenReturn(simpleModel);

        ResponseEntity<EntityModel<SimpleValue<UUID>>> response = bookingResource.createBooking(bookingDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(simpleModel);
        verify(bookingService).create(bookingDTO);
        verify(bookingAssembler).toSimpleModel(createdId);
    }

    @Test
    void updateBooking_returnsOk() {
        UUID id = UUID.randomUUID();
        BookingDTO bookingDTO = new BookingDTO();
        EntityModel<SimpleValue<UUID>> simpleModel = SimpleValue.entityModelOf(id);

        when(bookingAssembler.toSimpleModel(id)).thenReturn(simpleModel);

        ResponseEntity<EntityModel<SimpleValue<UUID>>> response = bookingResource.updateBooking(id, bookingDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(simpleModel);
        verify(bookingService).update(id, bookingDTO);
        verify(bookingAssembler).toSimpleModel(id);
    }

    @Test
    void cancelBooking_returnsNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = bookingResource.cancel(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(bookingService).cancel(id);
    }

    @Test
    void deleteBooking_returnsNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = bookingResource.deleteBooking(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(bookingService).delete(id);
    }

    @Test
    void getUserValues_returnsMap() {
        Map<Long, String> values = Map.of(1L, "User");
        when(userService.getUserValues()).thenReturn(values);

        ResponseEntity<Map<Long, String>> response = bookingResource.getUserValues();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(values);
        verify(userService).getUserValues();
    }

    @Test
    void getRoomValues_returnsMap() {
        Map<UUID, String> values = Map.of(UUID.randomUUID(), "Room");
        when(roomService.getRoomValues()).thenReturn(values);

        ResponseEntity<Map<UUID, String>> response = bookingResource.getRoomValues();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(values);
        verify(roomService).getRoomValues();
    }
}