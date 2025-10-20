package digi.booking.digi_booking_app.web;

import digi.booking.digi_booking_app.base.booking.BookingResource;
import digi.booking.digi_booking_app.base.room.RoomResource;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/v1/")
public class HomeResource {

    @GetMapping("/home")
    public RepresentationModel<?> index() {
        return RepresentationModel.of(null)
                .add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(BookingResource.class).getAllBookings(null, null)).withRel("bookings"))
                .add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(RoomResource.class).getAllRooms(null, null)).withRel("rooms"));
    }

}
