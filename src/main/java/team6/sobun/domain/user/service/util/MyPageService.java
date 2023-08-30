package team6.sobun.domain.user.service.util;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
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
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.domain.user.dto.find.FindEmailRequestDto;
import team6.sobun.domain.user.dto.find.FindEmailResponseDto;
import team6.sobun.domain.user.dto.find.PasswordFindRequestDto;
import team6.sobun.domain.user.dto.mypage.MypageRequestDto;
import team6.sobun.domain.user.dto.mypage.MypageResponseDto;
import team6.sobun.domain.user.dto.password.PasswordRequestDto;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.UserPopularityRepository;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.domain.user.service.UserService;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.stringCode.SuccessCodeEnum;
import team6.sobun.global.utils.ResponseUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MyPageService {

    @Value("${spring.mail.username}")
    private String from;

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender mailSender;
    private final S3Service s3Service;
    private final UserPopularityRepository userPopularityRepository;

    @Transactional
    public MypageResponseDto getCurrentUserDetails(Long requestingUserId) {
        Optional<User> optionalUser = userRepository.findById(requestingUserId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            List<Post> userPosts = userRepository.findPostsByUserId(requestingUserId);
            List<Post> pinedPost = userRepository.findPinnedPostsByUserId(requestingUserId);
            boolean isPopularity = false;
            // 조회한 정보를 DTO로 변환하여 리턴합니다.
            MypageResponseDto responseDto = new MypageResponseDto(
                    user.getId(),
                    user.getNickname(),
                    user.getProfileImageUrl(),
                    user.getPhoneNumber(),
                    user.isVerified(),
                    user.getPopularity(),
                    userPosts,
                    pinedPost,
                    user.getEmail(),
                    user.getLocation(),
                    isPopularity
            );
            return responseDto;
        } else {
            // 사용자를 찾지 못한 경우 에러 응답을 리턴합니다.
            throw new IllegalArgumentException();
        }
    }

    @Transactional
    public MypageResponseDto getUserDetails(Long userId, User user) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User findUser = optionalUser.get();
            List<Post> userPosts = userRepository.findPostsByUserId(userId);
            boolean isPopularity = false;
            if (userPopularityRepository.findByReceiverAndGiver(findUser, user) != null) {
                isPopularity = true;
            }

            // 조회한 정보를 DTO로 변환하여 리턴합니다.
            MypageResponseDto responseDto = new MypageResponseDto(
                    findUser.getId(),
                    findUser.getNickname(),
                    findUser.getProfileImageUrl(),
                    findUser.getPhoneNumber(),
                    findUser.isVerified(),
                    findUser.getPopularity(),
                    userPosts,
                    null,
                    findUser.getEmail(),
                    findUser.getLocation(),
                    isPopularity
            );
            return responseDto;
        } else {
            // 사용자를 찾지 못한 경우 에러 응답을 리턴합니다.
            throw new IllegalArgumentException();
        }
    }

    @Transactional
    public ApiResponse<?> updateUserProfile(Long userId, MypageRequestDto mypageRequestDto,
                                            User user, HttpServletResponse response) {
        log.info("'{}'님이 프로필 정보와 이미지를 변경했습니다.", user.getNickname());

        User existingUser = findUser(userId);

        if (!existingUser.getId().equals(user.getId())) {
            throw new IllegalArgumentException("동일한 사용자가 아닙니다.");
        }

        if (mypageRequestDto.getNickname() != null) {
            existingUser.updateNickname(mypageRequestDto.getNickname());
        }

        if (mypageRequestDto.getPhoneNumber() != null) {
            existingUser.updatePhoneNumber(mypageRequestDto.getPhoneNumber());
        }

        existingUser.update(mypageRequestDto);

        userService.addToken(existingUser, response);

        // 프로필 정보와 이미지 변경에 대한 성공 응답을 반환합니다.
        return ResponseUtils.okWithMessage(SuccessCodeEnum.USER_USERDATA_UPDATA_SUCCESS);
    }

    @Transactional
    public ApiResponse<?> findPassword(PasswordFindRequestDto requestDto) throws Exception {
        User user = userRepository.findByEmail(requestDto.getEmail()).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 이메일 입니다."));

        if (!requestDto.getUsername().equals(user.getUsername())) {
            throw new IllegalArgumentException("사용자 정보가 다릅니다.");
        }
        if (!requestDto.getPhoneNumber().equals(user.getPhoneNumber())) {
            throw new IllegalArgumentException("사용자 정보가 다릅니다.");
        }

        String changePassword = UUID.randomUUID().toString();
        changePassword = changePassword.substring(0, 8);

        // Thymeleaf를 사용하여 HTML 템플릿 생성
        String htmlContent = generateHtmlTemplate(changePassword);

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper h = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        h.setFrom(from);
        h.setTo(requestDto.getEmail());
        h.setSubject("임시 비밀번호 입니다.");
        h.setText(htmlContent, true);  // HTML 컨텐츠 설정
        mailSender.send(mimeMessage);

        String encodePassword = passwordEncoder.encode(changePassword);
        user.updatePassword(encodePassword);
        return ResponseUtils.okWithMessage(SuccessCodeEnum.PASSWORD_CHANGE_SUCCESS);
    }
    public FindEmailResponseDto findEmail(FindEmailRequestDto requestDto) {
        User user = userRepository.findByPhoneNumber(requestDto.getPhoneNumber())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전화번호 입니다.")
                );
        if (!requestDto.getUsername().equals(user.getUsername())) {
            throw new IllegalArgumentException("사용자 정보가 다릅니다.");
        }
        FindEmailResponseDto responseDto = new FindEmailResponseDto(user.getEmail());

        return responseDto;
    }

    private String generateHtmlTemplate(String tempPassword) {
        Context context = new Context();
        context.setVariable("tempPassword", tempPassword);
        return templateEngine.process("new_password_template", context);
    }

    public ApiResponse<?> updateUserProfileImage(Long userId, MultipartFile image, User user, HttpServletResponse response) {
        User existingUser = findUser(userId);

        if (!existingUser.getId().equals(user.getId())) {
            throw new IllegalArgumentException("동일한 사용자가 아닙니다.");
        }

        updateUserImageDetail(image, existingUser);
        userRepository.save(existingUser);

        userService.addToken(existingUser, response);
        return ResponseUtils.okWithMessage(SuccessCodeEnum.USER_USERDATA_UPDATA_SUCCESS);
    }
    private void updateUserImageDetail(MultipartFile image, User user) {
        String imageUrl = s3Service.upload(image);
        user.setProfileImageUrl(imageUrl);
        // 기존 게시물 프로필 이미지 유지를 위해 기존 이미지 삭제 X
    }


    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        }

    public ApiResponse<?> updateUserPassword(Long userId, PasswordRequestDto passwordRequestDto, User user) {
        User existingUser = findUser(userId);

        if (!existingUser.getId().equals(user.getId())) {
            throw new IllegalArgumentException("동일한 사용자가 아닙니다.");
        }
        if (passwordRequestDto.getPassword() != null) {
            String encodedPassword = passwordEncoder.encode(passwordRequestDto.getPassword());
            existingUser.updatePassword(encodedPassword);
        }
        return ResponseUtils.okWithMessage(SuccessCodeEnum.USER_USERDATA_UPDATA_SUCCESS);
    }
}
