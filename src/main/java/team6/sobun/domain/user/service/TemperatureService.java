package team6.sobun.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.UserRepository;

import java.util.List;

@Service
@Transactional
public class TemperatureService {

    private final UserRepository userRepository;

    public TemperatureService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

}