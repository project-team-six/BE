package team6.sobun.domain.user.service.util;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.sobun.domain.user.repository.UserRepository;

@Service
@Transactional
public class TemperatureService {

    private final UserRepository userRepository;

    public TemperatureService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

}