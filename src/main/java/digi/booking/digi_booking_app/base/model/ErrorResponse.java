package digi.booking.digi_booking_app.base.model;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String code;
    private String message;
    private List<FieldError> fieldErrors;

    public ErrorResponse(final String message) {
        this.message = message;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String code;
        private String message;
        private String property;
        private Object rejectedValue;
        private String path;
    }

}
