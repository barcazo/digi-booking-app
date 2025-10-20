package digi.booking.digi_booking_app.base.user;

import digi.booking.digi_booking_app.base.UserService;
import digi.booking.digi_booking_app.base.util.CustomCollectors;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Map<Long, String> getUserValues() {
        return userRepository.findAll(Sort.by("keycloakId"))
                .stream()
                .collect(CustomCollectors.toSortedMap(User::getKeycloakId, User::getEmail));
    }

}
