package digi.booking.digi_booking_app.base.model;

import lombok.Getter;
import org.springframework.hateoas.EntityModel;


@Getter
public class SimpleValue<T> {

    private final T value;

    private SimpleValue(final T value) {
        this.value = value;
    }

    public static <T> EntityModel<SimpleValue<T>> entityModelOf(final T value) {
        final SimpleValue<T> simpleValue = new SimpleValue<>(value);
        return EntityModel.of(simpleValue);
    }

}
