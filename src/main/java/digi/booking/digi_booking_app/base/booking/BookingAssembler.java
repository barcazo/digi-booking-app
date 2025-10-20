package digi.booking.digi_booking_app.base.booking;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import digi.booking.digi_booking_app.base.model.SimpleValue;
import digi.booking.digi_booking_app.base.room.RoomResource;
import java.util.UUID;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;


@Component
public class BookingAssembler implements SimpleRepresentationModelAssembler<BookingDTO> {

    @Override
    public void addLinks(final EntityModel<BookingDTO> entityModel) {
        entityModel.add(linkTo(methodOn(BookingResource.class).getBooking(entityModel.getContent().getId())).withSelfRel());
        entityModel.add(linkTo(methodOn(BookingResource.class).getAllBookings(null, null)).withRel(IanaLinkRelations.COLLECTION));
        entityModel.add(linkTo(methodOn(RoomResource.class).getRoom(entityModel.getContent().getRoom())).withRel("room"));
    }

    @Override
    public void addLinks(final CollectionModel<EntityModel<BookingDTO>> collectionModel) {
        collectionModel.add(linkTo(methodOn(BookingResource.class).getAllBookings(null, null)).withSelfRel());
    }

    public EntityModel<SimpleValue<UUID>> toSimpleModel(final UUID id) {
        final EntityModel<SimpleValue<UUID>> simpleModel = SimpleValue.entityModelOf(id);
        simpleModel.add(linkTo(methodOn(BookingResource.class).getBooking(id)).withSelfRel());
        return simpleModel;
    }

}
