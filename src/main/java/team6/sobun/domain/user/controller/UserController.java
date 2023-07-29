package team6.sobun.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.Api;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import team6.sobun.domain.user.dto.SignupRequestDto;
import team6.sobun.domain.user.dto.UserResponseDto;
import team6.sobun.domain.user.entity.Kakao;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.oauth.OauthToken;
import team6.sobun.domain.user.kakao.jwt.JwtProperties;
import team6.sobun.domain.user.service.KakaoService;
import team6.sobun.domain.user.service.UserService;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.UserDetailsImpl;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final KakaoService kakaoService;
    private final JwtProvider jwtProvider;

    /**
     * 회원 가입을 처리하는 메소드입니다.
     *
     * @param signupRequestDto 회원 가입 요청 DTO
     * @return 처리 결과에 대한 ApiResponse
     */

    @PostMapping("/signup")
    public ApiResponse<?> signup(@RequestBody @Valid SignupRequestDto signupRequestDto) {
        return userService.signup(signupRequestDto);
    }
    @DeleteMapping("/withdraw")
    public ApiResponse<?> withdraw(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.withdraw(userDetails.getUser());
    }

    @DeleteMapping("/logout")
    public ApiResponse<?> logout(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.logout(userDetails.getUser());
    }
    @PostMapping("/kakao")
    @Transactional
    public UserResponseDto kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        User user = userService.kakaoSignUpOrLinkUser(code);
        jwtProvider.addJwtHeader(jwtProvider.createToken(user.getNickname(),user.getRole()), response);
        return new UserResponseDto(user);
    }

    @GetMapping("/kakao")
    public ResponseEntity<Void> getKakao(HttpServletRequest request) {
        String redirectUri = "http://" + request.getHeader("host") + "/auth/token";
        URI uri = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com")
                .path("/oauth/authorize")
                .queryParam("client_id", kakaoService.clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        log.info("uri = " + uri.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uri);
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);

    }

    // 프론트에서 인가코드 받아오는 url
    @GetMapping("/token")
    public ResponseEntity getLogin(@RequestParam("code") String code, HttpServletRequest request) { //(1)

        String redirectUri = "http://" + request.getHeader("host") + "/auth/token";

        // 넘어온 인가 코드를 통해 access_token 발급
        OauthToken oauthToken = kakaoService.getAccessToken(code, redirectUri);

        //(2)
        // 발급 받은 accessToken 으로 카카오 회원 정보 DB 저장 후 JWT 를 생성
        String jwtToken = kakaoService.saveUserAndGetToken(oauthToken.getAccess_token());

        //(3)
        HttpHeaders headers = new HttpHeaders();
        headers.add(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);

        //(4)
        return ResponseEntity.ok().headers(headers).body("success");
    }


    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUser(HttpServletRequest request) {
        Kakao kakao = kakaoService.getUser(request);
        return ResponseEntity.ok().body(kakao);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 서비스 내에서 로그아웃 처리를 진행합니다.
        kakaoService.logout(request, response);

        // 로그아웃 완료 후, 다시 로그인 페이지로 리다이렉트합니다.
        String loginRedirectUrl = "http://" + request.getHeader("host") + "/auth/kakao";
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", loginRedirectUrl).build();
    }


}


