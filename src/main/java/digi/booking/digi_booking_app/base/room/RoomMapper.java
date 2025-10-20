package digi.booking.digi_booking_app.base.room;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;


@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RoomMapper {

    RoomDTO updateRoomDTO(Room room, @MappingTarget RoomDTO roomDTO);

    @Mapping(target = "id", ignore = true)
    Room updateRoom(RoomDTO roomDTO, @MappingTarget Room room);

}
