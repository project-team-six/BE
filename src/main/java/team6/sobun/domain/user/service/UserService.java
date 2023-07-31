package team6.sobun.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.sobun.domain.user.dto.MypageRequestDto;
import team6.sobun.domain.user.dto.SignupRequestDto;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.exception.InvalidConditionException;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.stringCode.ErrorCodeEnum;
import team6.sobun.global.stringCode.SuccessCodeEnum;
import team6.sobun.global.utils.ResponseUtils;

import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiResponse<?> signup(SignupRequestDto signupRequestDto) {
        String email = signupRequestDto.getEmail();
        String nickname = signupRequestDto.getNickname();
        String location = signupRequestDto.getLocation();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());
        UserRoleEnum role = UserRoleEnum.USER;

        checkDuplicatedEmail(email);

        User user = new User(email, nickname, password, location, role);
        userRepository.save(user);

        log.info("'{}' 이메일을 가진 사용자가 가입했습니다.", email);

        return ResponseUtils.okWithMessage(SuccessCodeEnum.USER_SIGNUP_SUCCESS);
    }


    private void checkDuplicatedEmail(String email) {
        Optional<User> found = userRepository.findByEmail(email);
        if (found.isPresent()) {
            throw new InvalidConditionException(ErrorCodeEnum.DUPLICATE_USERNAME_EXIST);
        }
    }

    @Transactional
    public ApiResponse<?> nicknameChange(Long id, MypageRequestDto mypageRequestDto, User user) {
        log.info("닉네임 변경 들어옴");
        User checkUser = userRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 사용자 입니다."));

        if (!checkUser.getId().equals(user.getId())) {
            throw new IllegalArgumentException("동일한 사용자가 아닙니다.");
        }

        checkUser.update(mypageRequestDto);
        return ResponseUtils.okWithMessage(SuccessCodeEnum.USER_NICKNAME_SUCCESS);
    }
}
