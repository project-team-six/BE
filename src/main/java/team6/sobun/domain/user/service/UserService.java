package team6.sobun.domain.user.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.pin.repository.PinRepository;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.post.repository.PostRepository;
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.domain.user.dto.*;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.exception.InvalidConditionException;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.repository.RefreshTokenRedisRepository;
import team6.sobun.global.stringCode.ErrorCodeEnum;
import team6.sobun.global.stringCode.SuccessCodeEnum;
import team6.sobun.global.utils.ResponseUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final JavaMailSender mailSender;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRedisRepository redisRepository;
    private final JwtProvider jwtProvider;
    private final S3Service s3Service;
    private final PinRepository pinRepository;


    @Value("${spring.mail.username}")
    private String from;


    public ApiResponse<?> signup(SignupRequestDto signupRequestDto, MultipartFile image) {
        String email = signupRequestDto.getEmail();
        String phoneNumber = signupRequestDto.getPhoneNumber();
        String location = "대구시";
        String username = signupRequestDto.getUsername();
        String nickname = signupRequestDto.getNickname();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());

        checkDuplicatedEmail(email);
        UserRoleEnum role = UserRoleEnum.USER;

        String profileImageUrl = null;
        if (image != null && !image.isEmpty()) {
            // 이미지가 있을 경우에만 S3에 업로드하고 URL을 가져옴
            profileImageUrl = s3Service.upload(image);
        }
        // 프로필 이미지 URL을 사용하여 User 객체 생성
        User user = new User(email, location, phoneNumber, nickname, password, username, profileImageUrl, role);
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
    public ApiResponse<?> updateUserNickname(Long id, MypageRequestDto mypageRequestDto, User user) {
        log.info("닉네임 변경 들어옴");
        User checkUser = userRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 사용자 입니다."));

        if (!checkUser.getId().equals(user.getId())) {
            throw new IllegalArgumentException("동일한 사용자가 아닙니다.");
        }

        checkUser.update(mypageRequestDto);
        return ResponseUtils.okWithMessage(SuccessCodeEnum.USER_NICKNAME_SUCCESS);
    }
    @Transactional
    public ApiResponse<?> updateUserImage(Long userId, MultipartFile image, User user) {
        log.info("'{}'님이 프로필 이미지를 변경했습니다.", user.getNickname());

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!existingUser.getId().equals(user.getId())) {
            throw new IllegalArgumentException("동일한 사용자가 아닙니다.");
        }

        // 이미지가 없을 경우 기존 이미지를 삭제 처리
        if (image == null || image.isEmpty()) {
            String existingImageUrl = user.getProfileImageUrl();
            if (StringUtils.hasText(existingImageUrl) && s3Service.fileExists(existingImageUrl)) {
                s3Service.delete(existingImageUrl);
                existingUser.setProfileImageUrl(null); // DB의 프로필 이미지 URL을 null로 설정
            }
        } else {
            updateUserImageDetail(image, existingUser);
        }

        userRepository.save(existingUser);

        // 프로필 이미지 변경에 대한 성공 응답을 반환합니다.
        return ResponseUtils.okWithMessage(SuccessCodeEnum.USER_IMAGE_SUCCESS);
    }

    private void updateUserImageDetail(MultipartFile image, User user) {
        String existingImageUrl = user.getProfileImageUrl();
        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.upload(image);
            user.setProfileImageUrl(imageUrl);

            // 기존 이미지가 존재하고 S3에 해당 파일이 있는 경우에만 삭제 처리
            if (StringUtils.hasText(existingImageUrl) && s3Service.fileExists(existingImageUrl)) {
                s3Service.delete(existingImageUrl);
            }
        } else {
            // 이미지가 없을 경우 기존 이미지를 삭제 처리
            if (StringUtils.hasText(existingImageUrl) && s3Service.fileExists(existingImageUrl)) {
                s3Service.delete(existingImageUrl);
                user.setProfileImageUrl(null); // DB의 프로필 이미지 URL을 null로 설정
            }
        }
    }



    public UserDetailResponseDto getUserDetails(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            List<Post> userPosts = userRepository.findPostsByUserId(userId);

            // 조회한 정보를 DTO로 변환하여 리턴합니다.
            UserDetailResponseDto responseDto = new UserDetailResponseDto(
                    user.getNickname(),
                    user.getProfileImageUrl(),
                    user.getMannerTemperature(),
                    userPosts,
                    null
            );
            return responseDto;
        } else {
            // 사용자를 찾지 못한 경우 에러 응답을 리턴합니다.
            throw new IllegalArgumentException();
        }
    }
    public UserDetailResponseDto getCurrentUserDetails(Long requestingUserId) {
        Optional<User> optionalUser = userRepository.findById(requestingUserId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            List<Post> userPosts = userRepository.findPostsByUserId(requestingUserId);
            List<Post> pinedPost = userRepository.findPinnedPostsByUserId(requestingUserId);

            // 조회한 정보를 DTO로 변환하여 리턴합니다.
            UserDetailResponseDto responseDto = new UserDetailResponseDto(
                    user.getNickname(),
                    user.getProfileImageUrl(),
                    user.getMannerTemperature(),
                    userPosts,
                    pinedPost
            );
            return responseDto;
        } else {
            // 사용자를 찾지 못한 경우 에러 응답을 리턴합니다.
            throw new IllegalArgumentException();
        }
    }

    @Transactional
    public ApiResponse<?> findPassword(PasswordRequestDto requestDto) throws Exception{
        User user = userRepository.findByEmail(requestDto.getEmail()).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 이메일 입니다."));

        if (!requestDto.getUsername().equals(user.getUsername())) {
            throw new IllegalArgumentException("사용자 정보가 다릅니다.");
        }
        if (!requestDto.getPhoneNumber().equals(user.getPhoneNumber())) {
            throw new IllegalArgumentException("사용자 정보가 다릅니다.");
        }

        String changePassword = UUID.randomUUID().toString();
        changePassword = changePassword.substring(0,8);

        MimeMessage m = mailSender.createMimeMessage();

        MimeMessageHelper h = new MimeMessageHelper(m,"UTF-8");
        h.setFrom("jgg7645@naver.com");
        h.setTo(requestDto.getEmail());
        h.setSubject("임시 비밀번호 입니다.");
        h.setText("임시 비밀번호 : " + changePassword);
        mailSender.send(m);

        String encodePassword = passwordEncoder.encode(changePassword);
        user.passwordUpdate(encodePassword);
        return ResponseUtils.okWithMessage(SuccessCodeEnum.PASSWORD_CHANGE_SUCCESS);
    }
}







