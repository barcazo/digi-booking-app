package digi.booking.digi_booking_app.base.room;

import digi.booking.digi_booking_app.base.RoomService;
import digi.booking.digi_booking_app.base.model.SimpleValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/v1/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoomResource {

    private final RoomService roomService;
    private final RoomAssembler roomAssembler;
    private final PagedResourcesAssembler<RoomDTO> pagedResourcesAssembler;

    public RoomResource(final RoomService roomService, final RoomAssembler roomAssembler,
            final PagedResourcesAssembler<RoomDTO> pagedResourcesAssembler) {
        this.roomService = roomService;
        this.roomAssembler = roomAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
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
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<PagedModel<EntityModel<RoomDTO>>> getAllRooms(
            @RequestParam(name = "filter", required = false) final String filter,
            @Parameter(hidden = true) @SortDefault(sort = "id") @PageableDefault(size = 20) final Pageable pageable) {
        final Page<RoomDTO> roomDTOs = roomService.findAll(filter, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(roomDTOs, roomAssembler));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<RoomDTO>> getRoom(@PathVariable(name = "id") final UUID id) {
        final RoomDTO roomDTO = roomService.get(id);
        return ResponseEntity.ok(roomAssembler.toModel(roomDTO));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<EntityModel<SimpleValue<UUID>>> createRoom(
            @RequestBody @Valid final RoomDTO roomDTO) {
        final UUID createdId = roomService.create(roomDTO);
        return new ResponseEntity<>(roomAssembler.toSimpleModel(createdId), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<SimpleValue<UUID>>> updateRoom(
            @PathVariable(name = "id") final UUID id, @RequestBody @Valid final RoomDTO roomDTO) {
        roomService.update(id, roomDTO);
        return ResponseEntity.ok(roomAssembler.toSimpleModel(id));
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteRoom(@PathVariable(name = "id") final UUID id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
