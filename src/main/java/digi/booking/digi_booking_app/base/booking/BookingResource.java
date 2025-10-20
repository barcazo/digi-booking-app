package digi.booking.digi_booking_app.base.booking;

import digi.booking.digi_booking_app.base.BookingService;
import digi.booking.digi_booking_app.base.RoomService;
import digi.booking.digi_booking_app.base.UserService;
import digi.booking.digi_booking_app.base.model.SimpleValue;
import digi.booking.digi_booking_app.base.security.UserRoles;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/v1/v1/bookings", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearer-jwt")
public class BookingResource {

    private final BookingService bookingService;
    private final BookingAssembler bookingAssembler;
    private final PagedResourcesAssembler<BookingDTO> pagedResourcesAssembler;
    private final UserService userService;
    private final RoomService roomService;

    public BookingResource(final BookingService bookingService,
            final BookingAssembler bookingAssembler,
            final PagedResourcesAssembler<BookingDTO> pagedResourcesAssembler,
            final UserService userService, final RoomService roomService) {
        this.bookingService = bookingService;
        this.bookingAssembler = bookingAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.userService = userService;
        this.roomService = roomService;
    }

    @Operation(
            parameters = {
                    @Parameter(
                            name = "page",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = Integer.class)
                    ),
                    @Parameter(
                            name = "size",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = Integer.class)
                    ),
                    @Parameter(
                            name = "sort",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = String.class)
                    )
            }
    )
    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + UserRoles.ADMIN + "', '" + UserRoles.USER + "')")
    public ResponseEntity<PagedModel<EntityModel<BookingDTO>>> getAllBookings(
            @RequestParam(name = "filter", required = false) final String filter,
            @Parameter(hidden = true) @SortDefault(sort = "id") @PageableDefault(size = 20) final Pageable pageable) {
        final Page<BookingDTO> bookingDTOs = bookingService.findAll(filter, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(bookingDTOs, bookingAssembler));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public ResponseEntity<EntityModel<BookingDTO>> getBooking(
            @PathVariable(name = "id") final UUID id) {
        final BookingDTO bookingDTO = bookingService.get(id);
        return ResponseEntity.ok(bookingAssembler.toModel(bookingDTO));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<EntityModel<SimpleValue<UUID>>> createBooking(
            @RequestBody @Valid final BookingDTO bookingDTO) {
        final UUID createdId = bookingService.create(bookingDTO);
        return new ResponseEntity<>(bookingAssembler.toSimpleModel(createdId), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public ResponseEntity<EntityModel<SimpleValue<UUID>>> updateBooking(
            @PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final BookingDTO bookingDTO) {
        bookingService.update(id, bookingDTO);
        return ResponseEntity.ok(bookingAssembler.toSimpleModel(id));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> cancel(@PathVariable(name = "id") final UUID id) {
        bookingService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteBooking(@PathVariable(name = "id") final UUID id) {
        bookingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/userValues")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public ResponseEntity<Map<Long, String>> getUserValues() {
        return ResponseEntity.ok(userService.getUserValues());
    }

    @GetMapping("/roomValues")
    @PreAuthorize("hasAuthority('" + UserRoles.ADMIN + "')")
    public ResponseEntity<Map<UUID, String>> getRoomValues() {
        return ResponseEntity.ok(roomService.getRoomValues());
    }

}
