package digi.booking.digi_booking_app.base.room;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RoomDTO {

    private UUID id;

    @NotNull
    private Integer roomNumber;

    @NotNull
    @Size(max = 255)
    private String roomType;

    @NotNull
    private Integer capacity;

    @NotNull
    @Digits(integer = 10, fraction = 2)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(type = "string", example = "75.08")
    private BigDecimal price;

    @NotNull
    @Size(max = 255)
    private String amenities;

    private Boolean active;

}
