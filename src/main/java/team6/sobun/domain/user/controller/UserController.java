package team6.sobun.domain.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.pin.repository.PinRepository;
import team6.sobun.domain.user.dto.MypageRequestDto;
import team6.sobun.domain.user.dto.SignupRequestDto;
import team6.sobun.domain.user.entity.RefreshToken;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.domain.user.service.UserService;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.UserDetailsImpl;
import team6.sobun.global.security.repository.RefreshTokenRedisRepository;
import team6.sobun.global.stringCode.SuccessCodeEnum;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {


    @Value("${kakao.login.client.id}")
    private String YOUR_KAKAO_LOGIN_CLIENT_ID;

    @Value("${kakao.login.redirect.uri}")
    private String YOUR_REDIRECT_URI;

    @Value("${naver.login.client.id}")
    private String YOUR_NAVER_LOGIN_CLIENT_ID;

    @Value("${naver.login.redirect.uri}")
    private String YOUR_NAVER_REDIRECT_URI;

    @Value("${facebook.login.client.id}")
    private String YOUR_FACEBOOK_LOGIN_CLIENT_ID;

    @Value("${facebook.login.redirect.uri}")
    private String YOUR_FACEBOOK_REDIRECT_URI;

    @Value("${google.login.client.id}")
    private String YOUR_GOOGLE_LOGIN_CLIENT_ID;

    @Value("${google.login.redirect.uri}")
    private String YOUR_GOOGLE_REDIRECT_URI;




    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final PinRepository pinRepository;
    private final RefreshTokenRedisRepository redisRepository;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ApiResponse<?> signup(@RequestPart(value = "data") SignupRequestDto signupRequestDto,
                                 @RequestPart(value = "file", required = false) MultipartFile image) {
        return userService.signup(signupRequestDto, image);
    }

    @GetMapping("/mypage/{userid}")
    public ApiResponse<?> userDetailView(@PathVariable Long userid, HttpServletRequest req) {
        return userService.getUserDetails(userid);
    }

    @PatchMapping("/mypage/{userid}")
    public ApiResponse<?> nicknameChange(@PathVariable Long userid,
                                         @RequestBody MypageRequestDto mypageRequestDto,
                                         @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return userService.nicknameChange(userid, mypageRequestDto, userDetailsImpl.getUser());
    }

    @DeleteMapping("/withdraw")
    public ApiResponse<?> withdraw(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.withdraw(userDetails.getUser());
    }

    @DeleteMapping("/logout")
    public ApiResponse<?> logout(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.logout(userDetails.getUser());
    }

    @GetMapping("/kakao")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        String kakaoLoginUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + YOUR_KAKAO_LOGIN_CLIENT_ID
                + "&redirect_uri=" + YOUR_REDIRECT_URI
                + "&response_type=code";
        response.sendRedirect(kakaoLoginUrl);
    }

    @Transactional
    @GetMapping("/kakao/login")
    public ApiResponse<?> kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        log.info("카카오 로그인 콜백 요청 받음. 인증 코드: {}", code);

        // 카카오 로그인에 성공한 후, 사용자 정보 가져오기
        User user = userService.kakaoSignUpOrLinkUser(code);
        log.info("카카오 로그인 성공. 유저 ID: {}", user.getId());
        String token = jwtProvider.createToken(user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken();
        jwtProvider.addJwtHeader(token, response);

        // refresh 토큰은 redis에 저장
        RefreshToken refresh = RefreshToken.builder()
                .id(user.getEmail())
                .token(token)
                .refreshToken(refreshToken)
                .build();
        log.info("리프레쉬 토큰 저장 성공. 유저 ID: {}", user.getId());
        redisRepository.save(refresh);

        log.info("JWT 토큰을 쿠키에 추가하여 응답합니다.");

        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);
    }


    @GetMapping("/naver")
    public void naverLogin(HttpServletResponse response) throws IOException {
        String naverLoginUrl = "https://nid.naver.com/oauth2.0/authorize"
                + "?client_id=" + YOUR_NAVER_LOGIN_CLIENT_ID
                + "&redirect_uri=" + YOUR_NAVER_REDIRECT_URI
                + "&response_type=code";
        response.sendRedirect(naverLoginUrl);
    }

    @Transactional
    @GetMapping("/naver/login")
    public ApiResponse<?> naverCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        log.info("네이버 로그인 콜백 요청 받음. 인증 코드: {}", code);

        // 네이버 로그인에 성공한 후, 사용자 정보 가져오기
        User user = userService.naverSignUpOrLinkUser(code);
        log.info("네이버 로그인 성공. 유저 ID: {}", user.getId());
        String token = jwtProvider.createToken(user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken();
        jwtProvider.addJwtHeader(token, response);

        // refresh 토큰은 redis에 저장
        RefreshToken refresh = RefreshToken.builder()
                .id(user.getEmail())
                .token(token)
                .refreshToken(refreshToken)
                .build();
        log.info("리프레쉬 토큰 저장 성공. 유저 ID: {}", user.getId());
        redisRepository.save(refresh);

        log.info("JWT 토큰을 쿠키에 추가하여 응답합니다.");

        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);
    }

    @GetMapping("/facebook")
    public void facebookLogin(HttpServletResponse response) throws IOException {
        String facebookLoginUrl = "https://www.facebook.com/v11.0/dialog/oauth"
                + "?client_id=" + YOUR_FACEBOOK_LOGIN_CLIENT_ID
                + "&redirect_uri=" + YOUR_FACEBOOK_REDIRECT_URI
                + "&scope=email" // 필요한 권한에 따라 scope 설정
                + "&response_type=code";
        response.sendRedirect(facebookLoginUrl);
    }

    @GetMapping("/facebook/login")
    public ApiResponse<?> facebookCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        log.info("페이스북 로그인 콜백 요청 받음. 인증 코드: {}", code);

        // 페이스북 로그인에 성공한 후, 사용자 정보 가져오기
        User user = userService.facebookSignUpOrLinkUser(code);
        log.info("페이스북 로그인 성공. 유저 ID: {}", user.getId());
        String token = jwtProvider.createToken(user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken();
        jwtProvider.addJwtHeader(token, response);

        // refresh 토큰은 redis에 저장
        RefreshToken refresh = RefreshToken.builder()
                .id(user.getEmail())
                .token(token)
                .refreshToken(refreshToken)
                .build();
        log.info("리프레쉬 토큰 저장 성공. 유저 ID: {}", user.getId());
        redisRepository.save(refresh);

        log.info("JWT 토큰을 쿠키에 추가하여 응답합니다.");

        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);
    }

    @GetMapping("/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        String googleLoginUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + YOUR_GOOGLE_LOGIN_CLIENT_ID
                + "&redirect_uri=" + YOUR_GOOGLE_REDIRECT_URI
                + "&response_type=code"
                + "&scope=openid%20email%20profile";
        response.sendRedirect(googleLoginUrl);
    }

    @Transactional
    @GetMapping("/google/login")
    public ApiResponse<?> googleCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        log.info("구글 로그인 콜백 요청 받음. 인증 코드: {}", code);

        // 구글 로그인에 성공한 후, 사용자 정보 가져오기
        User user = userService.googleSignUpOrLinkUser(code);
        log.info("구글 로그인 성공. 유저 ID: {}", user.getId());
        String token = jwtProvider.createToken(user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken();
        jwtProvider.addJwtHeader(token, response);

        // refresh 토큰은 redis에 저장
        RefreshToken refresh = RefreshToken.builder()
                .id(user.getEmail())
                .token(token)
                .refreshToken(refreshToken)
                .build();
        log.info("리프레쉬 토큰 저장 성공. 유저 ID: {}", user.getId());
        redisRepository.save(refresh);

        log.info("JWT 토큰을 쿠키에   추가하여 응답합니다.");

        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);
    }
}





