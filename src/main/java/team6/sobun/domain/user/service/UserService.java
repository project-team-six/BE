package team6.sobun.domain.user.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.spring6.SpringTemplateEngine;
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.domain.user.dto.SignupRequestDto;
import team6.sobun.domain.user.dto.mypage.MypageRequestDto;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.domain.user.repository.mypage.MypageRepository;
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


    public ApiResponse<?> signup(SignupRequestDto signupRequestDto, MultipartFile image) {
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
        }
        // 프로필 이미지 URL을 사용하여 User 객체 생성
        User user = new User(email, phoneNumber, nickname, password, username, profileImageUrl, role);
        userRepository.save(user);

        log.info("'{}' 이메일을 가진 사용자가 가입했습니다.", email);

        return ResponseUtils.okWithMessage(SuccessCodeEnum.USER_SIGNUP_SUCCESS);
    }

    public ApiResponse<?> logout(String token,HttpServletResponse response) {
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
            }return ApiResponse.success("로그아웃 성공");
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
        String token = jwtProvider.createToken(String.valueOf(user.getId()),user.getEmail(), user.getNickname(), user.getRole(),
                user.getProfileImageUrl(), user.getLocation().myAddress(user.getLocation().getSido(), user.getLocation().getSigungu(), user.getLocation().getDong()));
        String refreshToken = jwtProvider.createRefreshToken(String.valueOf(user.getId()),user.getEmail(), user.getNickname(), user.getRole(),
                user.getProfileImageUrl(), user.getLocation().myAddress(user.getLocation().getSido(), user.getLocation().getSigungu(), user.getLocation().getDong()));

        jwtProvider.addJwtHeaders(token,refreshToken, response);

        // refresh 토큰은 redis에 저장
        RefreshToken refresh = RefreshToken.builder()
                .id(user.getEmail())
                .refreshToken(refreshToken)
                .build();
        log.info("리프레쉬 토큰 저장 성공. 유저 ID: {}", user.getId());
        redisRepository.save(refresh);
        return ApiResponse.success("토큰 발급 성공 !");
    }
}







