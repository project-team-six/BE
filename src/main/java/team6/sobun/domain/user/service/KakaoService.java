package team6.sobun.domain.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import team6.sobun.domain.user.entity.Kakao;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.oauth.KakaoProfile;
import team6.sobun.domain.user.entity.oauth.OauthToken;
import team6.sobun.domain.user.repository.KakaoRepository;

import java.io.IOException;
import java.util.Date;

@Slf4j
@Service
public class KakaoService {

    private final KakaoRepository kakaoRepository;
    private final RestTemplate restTemplate;


    @Value("${kakao.client-id}")
    public String clientId;

    @Value("${kakao.client-secret}")
    public String clientSecret;

    public KakaoService(KakaoRepository kakaoRepository, RestTemplateBuilder restTemplateBuilder) {
        this.kakaoRepository = kakaoRepository;
        this.restTemplate = restTemplateBuilder.build();
    }

    public String saveUserAndGetToken(String token) { //(1)
        KakaoProfile profile = findProfile(token);

        Kakao kakao = kakaoRepository.findByKakaoEmail(profile.getKakao_account().getEmail());
        if (kakao == null) {
            kakao = Kakao.builder()
                    .kakaoId(profile.getId())
                    .kakaoProfileImg(profile.getKakao_account().getProfile().getProfile_image_url())
                    .kakaoNickname(profile.getKakao_account().getProfile().getNickname())
                    .kakaoEmail(profile.getKakao_account().getEmail())
                    .userRole("ROLE_USER").build();

            kakaoRepository.save(kakao);
        }

        return createToken(kakao); //(2)
    }


    public String createToken(Kakao kakao) {
        String jwtToken =
                Jwts.builder()
                        .setSubject(kakao.getKakaoEmail())
                        .setExpiration(new Date(System.currentTimeMillis()))
                        .claim("id", kakao.getKakaoId())
                        .claim("nickname", kakao.getKakaoId())
                        .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS512))
                        .compact();

        return jwtToken;
    }

    public KakaoProfile findProfile(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest =
                new HttpEntity<>(headers);

        ResponseEntity<String> kakaoProfileResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoProfile kakaoProfile = null;
        try {
            kakaoProfile = objectMapper.readValue(kakaoProfileResponse.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return kakaoProfile;
    }

    public OauthToken getAccessToken(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("grant_type", "authorization_code");
        payload.add("client_id", clientId);
        payload.add("redirect_uri", redirectUri);
        payload.add("code", code);
        payload.add("client_secret", clientSecret); // 사용할 Client Secret 추가

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(payload, headers);

        ResponseEntity<OauthToken> accessTokenResponse = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                OauthToken.class
        );

        return accessTokenResponse.getBody();
    }

    public Kakao getUser(HttpServletRequest request) {
        Long userCode = (Long) request.getAttribute("userCode");
        Kakao kakao = kakaoRepository.findByKakaoId(userCode);
        return kakao;
    }

    public String getKakaoLogoutUrl(HttpServletRequest request) {
        String redirectUri = "http://" + request.getHeader("host") + "/auth/logout";
        return "https://kauth.kakao.com/oauth/logout?client_id=" + clientId + "&logout_redirect_uri=" + redirectUri;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        //  클라이언트 쿠키 삭제
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }

        // 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // 카카오 로그아웃
        String kakaoLogoutUrl = getKakaoLogoutUrl(request);

        // 브라우저로 응답을 보내지 않도록 처리
        try {
            response.sendRedirect(kakaoLogoutUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 데이터베이스에서 사용자 정보 제거
        Long userCode = (Long) request.getAttribute("userCode");
        if (userCode != null) {
            kakaoRepository.deleteById(userCode);
        }
    }
}
