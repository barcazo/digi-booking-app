package digi.booking.digi_booking_app.base.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setKeycloakId(101L);
        user.setEmail("user@example.com");
    }

    @Test
    void getUserValues_returnsSortedMap() {
        when(userRepository.findAll(Sort.by("keycloakId"))).thenReturn(List.of(user));

        Map<Long, String> result = userService.getUserValues();

        assertThat(result).containsEntry(user.getKeycloakId(), user.getEmail());
        verify(userRepository).findAll(Sort.by("keycloakId"));
    }
}