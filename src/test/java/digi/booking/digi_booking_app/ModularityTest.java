package digi.booking.digi_booking_app;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;


public class ModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(DigiBookingAppApplication.class);

    @Test
    void verifyModuleStructure() {
        modules.verify();
    }

    /**
     * Generates documentation in target/spring-modulith-docs.
     */
    @Test
    void createModuleDocumentation() {
        new Documenter(modules).writeDocumentation();
    }

}
