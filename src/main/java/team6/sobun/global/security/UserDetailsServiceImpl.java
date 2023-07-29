package team6.sobun.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자의 세부 정보를 로드합니다.
     *
     * @param email 사용자 이름
     * @return UserDetails 객체
     * @throws UsernameNotFoundException 주어진 사용자 이름이 존재하지 않을 경우 예외가 발생합니다.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 " + email + "은 존재하지 않습니다"));

        return new UserDetailsImpl(user);
    }
}
