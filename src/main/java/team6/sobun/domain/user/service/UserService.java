package team6.sobun.domain.user.service;

import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.domain.user.dto.SignupRequestDto;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.domain.user.service.util.IdenticonService;
import team6.sobun.global.exception.InvalidConditionException;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.jwt.entity.RefreshToken;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.repository.RefreshTokenRedisRepository;
import team6.sobun.global.stringCode.ErrorCodeEnum;
import team6.sobun.global.stringCode.SuccessCodeEnum;
import team6.sobun.global.utils.ResponseUtils;

import java.net.URLDecoder;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRedisRepository redisRepository;
    private final JwtProvider jwtProvider;
    private final S3Service s3Service;
    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender mailSender;
    private final RedisTemplate redisTemplate;
    private final RedisChatRepository redisChatRepository;
    private final IdenticonService identiconService;

    @Value("${spring.mail.username}")
    private String from;


    public ApiResponse<?> signup(SignupRequestDto signupRequestDto, MultipartFile image) throws MessagingException {
        String email = signupRequestDto.getEmail();
        String phoneNumber = signupRequestDto.getPhoneNumber();
        String username = signupRequestDto.getUsername();
        String nickname = signupRequestDto.getNickname();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());

        checkDuplicatedEmail(email);
        UserRoleEnum role = UserRoleEnum.USER;

        String profileImageUrl = null;
        if (image != null && !image.isEmpty()) {
            // 이미지가 있을 경우에만 S3에 업로드하고 URL을 가져옴
            profileImageUrl = s3Service.upload(image);
        } else {
            profileImageUrl = identiconService.makeIdenticonUrl(signupRequestDto.getNickname());
        }

        // 프로필 이미지 URL을 사용하여 User 객체 생성
        User user = new User(email, phoneNumber, nickname, password, username, profileImageUrl, role);
        String verificationToken = UUID.randomUUID().toString();

        saveVerificationTokenToRedis(email, verificationToken);
        log.info("레디스에 이메일 토큰 저장되었나? = {}", verificationToken);

        userRepository.save(user);
        sendVerificationEmail(user.getEmail(), verificationToken);
        log.info("'{}' 이메일을 가진 사용자가 가입했습니다.", email);

        return ResponseUtils.okWithMessage(SuccessCodeEnum.USER_SIGNUP_SUCCESS);
    }

    public ApiResponse<?> logout(String token, HttpServletResponse response) {
        try {
            // 디코딩된 토큰 추출
            String decodedToken = URLDecoder.decode(token, "UTF-8");
            // "Bearer " 제거
            String cleanToken = decodedToken.replace("Bearer ", "");
            Claims claims = jwtProvider.getUserInfoFromToken(cleanToken);
            if (claims != null) {
                String refreshTokenKey = claims.getSubject();
                log.info("레디스에서 리프레시 토큰 삭제 시도: " + refreshTokenKey);
                redisRepository.deleteById(refreshTokenKey);
                log.info("레디스에서 리프레시 토큰 삭제 성공");
                jwtProvider.expireAccessToken(token, response);
            }
            return ApiResponse.success("로그아웃 성공");
        } catch (Exception e) {
            log.error("로그아웃 실패: {}", e.getMessage());
            throw new RuntimeException("로그아웃 실패");
        }
    }


    private void checkDuplicatedEmail(String email) {
        Optional<User> found = userRepository.findByEmail(email);
        if (found.isPresent()) {
            throw new InvalidConditionException(ErrorCodeEnum.DUPLICATE_USERNAME_EXIST);
        }
    }

    // 소셜 로그인시 토큰생성 로직
    public ApiResponse<?> addToken(User user, HttpServletResponse response) {
        String token = jwtProvider.createToken(String.valueOf(user.getId()), user.getEmail(), user.getNickname(), user.getRole(),
                user.getProfileImageUrl(), user.getLocation() == null ? "서울시 구로구 가산동" : user.getLocation().myAddress(user.getLocation().getSido(), user.getLocation().getSigungu(), user.getLocation().getDong()));
        String refreshToken = jwtProvider.createRefreshToken(String.valueOf(user.getId()), user.getEmail(), user.getNickname(), user.getRole(),
                user.getProfileImageUrl(), user.getLocation() == null ? "서울시 구로구 가산동" : user.getLocation().myAddress(user.getLocation().getSido(), user.getLocation().getSigungu(), user.getLocation().getDong()));

        jwtProvider.addJwtHeaders(token, refreshToken, response);

        // refresh 토큰은 redis에 저장
        RefreshToken refresh = RefreshToken.builder()
                .id(user.getEmail())
                .refreshToken(refreshToken)
                .build();
        log.info("리프레쉬 토큰 저장 성공. 유저 ID: {}", user.getId());
        redisRepository.save(refresh);
        return ApiResponse.success("토큰 발급 성공 !");
    }

    public ApiResponse<?> makeUserAdmin(Long userId) {
        User user = findUserById(userId);
        user.setRole(UserRoleEnum.ADMIN);
        userRepository.save(user);
        return ApiResponse.success("사용자 권한을 ADMIN으로 변경하였습니다.");
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new InvalidConditionException(ErrorCodeEnum.USER_NOT_EXIST));
    }

    private void sendVerificationEmail(String toEmail, String verificationToken) throws MessagingException {
        // Thymeleaf를 사용하여 이메일 내용 생성
        String htmlContent = generateVerificationHtmlTemplate(verificationToken);

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper h = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        h.setFrom(from);
        h.setTo(toEmail);
        h.setSubject("회원가입 이메일 인증");
        h.setText(htmlContent, true);  // HTML 컨텐츠 설정
        mailSender.send(mimeMessage);
    }

    private String generateVerificationHtmlTemplate(String verificationToken) {
        Context context = new Context();
        context.setVariable("verificationToken", verificationToken);

        // 사용자가 확인할 수 있는 링크 생성
        String verificationLink = "http://localhost:8080/auth/email?verificationToken=" + verificationToken;
        context.setVariable("verificationLink", verificationLink);

        return templateEngine.process("verificationTemplate", context);
    }

    // 이메일 인증 시에 레디스에서 토큰 확인 및 삭제
    public ApiResponse<?> verifyEmail(String verificationToken) {
        String email = redisChatRepository.findUserIdByVerificationToken(verificationToken);
        if (email == null) {
            // 레디스에서 토큰을 찾지 못한 경우 처리
            throw new IllegalArgumentException("유효하지 않은 인증 토큰입니다.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 Email입니다."));
        if (user.isVerified()) {
            return ApiResponse.success("이미 인증된 사용자입니다.");
        }
        // 토큰 확인 후 인증 상태 변경
        user.setVerified(true);
        userRepository.save(user);
        // 레디스에서 토큰 삭제
        redisChatRepository.deleteVerificationToken(verificationToken);
        return ResponseUtils.okWithMessage(SuccessCodeEnum.EMAIL_VERIFICATION_SUCCESS);
    }


    private void saveVerificationTokenToRedis(String email, String verificationToken) {
        if (verificationToken != null) {
            String redisKey = "verificationToken:" + verificationToken; // 키 수정
            redisTemplate.opsForValue().set(redisKey, email); // 값으로 이메일을 매핑하여 저장
            // 설정된 만료 시간 동안 저장
            redisTemplate.expire(redisKey, 1, TimeUnit.HOURS); // 1시간 동안 유효
        } else {
            log.error("verificationToken 값이 null이므로 Redis에 저장할 수 없습니다.");
        }
    }

    public ApiResponse<?> makeUserBlack(Long userId) {
        User user = findUserById(userId);
        user.setRole(UserRoleEnum.BLACK);
        userRepository.save(user);
        return ApiResponse.success(user.getNickname() + " 유저의 권한을 제한 하였습니다.");
    }

    public ApiResponse<?> makeUser(Long userId) {
        User user = findUserById(userId);
        user.setRole(UserRoleEnum.USER);
        userRepository.save(user);
        return ApiResponse.success(user.getNickname() + " 유저의 권한을 활성화 하였습니다.");
    }
}








