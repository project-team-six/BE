package team6.sobun.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.user.dto.MypageRequestDto;
import team6.sobun.domain.user.dto.SignupRequestDto;
import team6.sobun.domain.user.entity.RefreshToken;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.service.UserService;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.UserDetailsImpl;
import team6.sobun.global.security.repository.RefreshTokenRedisRepository;
import team6.sobun.global.stringCode.SuccessCodeEnum;

import java.io.IOException;

@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {


    @Value("${kakao.login.client.id}")
    private String YOUR_KAKAO_LOGIN_CLIENT_ID;

    @Value("${kakao.login.redirect.uri}")
    private String YOUR_REDIRECT_URI;

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRedisRepository redisRepository;

    @PostMapping("/signup")
    public ApiResponse<?> signup(@RequestPart(value = "data") SignupRequestDto signupRequestDto,
                                 @RequestPart(value = "file", required = false) MultipartFile image) {
        return userService.signup(signupRequestDto, image);
    }

    @PatchMapping("/mypage/{id}")
    public ApiResponse<?> nicknameChange(@PathVariable Long id,
                                         @RequestBody MypageRequestDto mypageRequestDto,
                                         @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return userService.nicknameChange(id, mypageRequestDto, userDetailsImpl.getUser());
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
}






