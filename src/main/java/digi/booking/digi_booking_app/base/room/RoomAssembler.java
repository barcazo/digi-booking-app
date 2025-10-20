package digi.booking.digi_booking_app.base.room;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import digi.booking.digi_booking_app.base.model.SimpleValue;
import java.util.UUID;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;


@Component
public class RoomAssembler implements SimpleRepresentationModelAssembler<RoomDTO> {

    @Override
    public void addLinks(final EntityModel<RoomDTO> entityModel) {
        entityModel.add(linkTo(methodOn(RoomResource.class).getRoom(entityModel.getContent().getId())).withSelfRel());
        entityModel.add(linkTo(methodOn(RoomResource.class).getAllRooms(null, null)).withRel(IanaLinkRelations.COLLECTION));
    }

    @Override
    public void addLinks(final CollectionModel<EntityModel<RoomDTO>> collectionModel) {
        collectionModel.add(linkTo(methodOn(RoomResource.class).getAllRooms(null, null)).withSelfRel());
    }

    public EntityModel<SimpleValue<UUID>> toSimpleModel(final UUID id) {
        final EntityModel<SimpleValue<UUID>> simpleModel = SimpleValue.entityModelOf(id);
        simpleModel.add(linkTo(methodOn(RoomResource.class).getRoom(id)).withSelfRel());
        return simpleModel;
    }

}
