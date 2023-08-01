package team6.sobun.domain.user.service.social;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.domain.user.dto.social.FacebookUserInfo;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.domain.user.service.IdenticonService;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.security.repository.RefreshTokenRedisRepository;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional

public class FacebookService {

    private final UserRepository userRepository;
    private final IdenticonService identiconService; // IdenticonService 주입
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRedisRepository redisRepository;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;
    private final S3Service s3Service;


    @Value("${facebook.login.callback.url}")
    private String facebookCallbackUrl;

    @Value("${facebook.login.client.id}")
    private String facebookLoginClientId;

    @Value("${facebook.api.secret-key}")
    private String facebookApiSecretKey;


    public User facebookSignUpOrLinkUser(String code) throws JsonProcessingException {
        log.info("페이스북 로그인 시도 중. 인증 코드: {}", code);

        String accessToken = getFacebookToken(code);
        log.info("페이스북 서버에서 토큰 받기 성공적. 액세스 토큰: {}", accessToken);

        FacebookUserInfo facebookUserInfo = getFacebookUserInfo(accessToken);
        log.info("페이스북 사용자 정보: id={}, name={}, email={}, picture={}",
                facebookUserInfo.getId(), facebookUserInfo.getName(), facebookUserInfo.getEmail(), facebookUserInfo.getPicture());

        User user = userRepository.findByEmail(facebookUserInfo.getEmail()).orElse(null);
        if (user == null) {
            log.info("새로운 페이스북 사용자 등록을 진행합니다.");
            user = registerFacebookUser(facebookUserInfo);
        } else {
            log.info("기존 사용자와 연결되는 페이스북 사용자로 등록합니다.");
            // 페이스북 사용자와 기존 사용자를 연결하거나 필요한 정보를 업데이트하는 로직 추가 (예: 프로필 이미지 URL 업데이트 등)
            user.setNickname(facebookUserInfo.getName());
            user.setProfileImageUrl(facebookUserInfo.getPicture());
        }
        return user;
    }

    private FacebookUserInfo getFacebookUserInfo(String accessToken) throws JsonProcessingException {
        URI uri = UriComponentsBuilder
                .fromUriString("https://graph.facebook.com/me")
                .queryParam("fields", "id,name,email,picture")
                .queryParam("access_token", accessToken)
                .encode()
                .build()
                .toUri();

        ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                FacebookUserInfo facebookUserInfo = new FacebookUserInfo();
                facebookUserInfo.setId(jsonNode.get("id").asText());
                facebookUserInfo.setName(jsonNode.get("name").asText());
                facebookUserInfo.setEmail(jsonNode.get("email").asText());
                facebookUserInfo.setPicture(jsonNode.get("picture").get("data").get("url").asText());
                return facebookUserInfo;
            } catch (JsonProcessingException e) {
                log.error("페이스북 API 응답 파싱 오류.", e);
                throw new RuntimeException("페이스북 API 응답 파싱 오류.", e);
            }
        } else {
            log.error("페이스북 인증 코드로 사용자 정보를 가져오는 중 오류 발생. 응답: {}", response.getBody());
            throw new RuntimeException("페이스북 인증 코드로 사용자 정보를 가져오는 중 오류 발생.");
        }
    }
    public User registerFacebookUser(FacebookUserInfo facebookUserInfo) {
        User user = userRepository.findByEmail(facebookUserInfo.getEmail()).orElse(null);
        if (user == null) {
            // 사용자가 존재하지 않으면 새로운 사용자를 생성합니다.
            String randomPwd = passwordEncoder.encode(String.valueOf(facebookUserInfo.getId()));
            user = User.builder()
                    .email(facebookUserInfo.getEmail())
                    .nickname(facebookUserInfo.getName())
                    .password(randomPwd)
                    .profileImageUrl(facebookUserInfo.getPicture())
                    .role(UserRoleEnum.USER)
                    .build();
            userRepository.save(user);
        } else {
            // 사용자가 이미 존재하면 프로필 정보를 업데이트합니다.
            user.setNickname(facebookUserInfo.getName());
            if (facebookUserInfo.getPicture() != null) {
                user.setProfileImageUrl(facebookUserInfo.getPicture());
            }
        }
        return user;
    }
    public String getFacebookToken(String code) {
        String facebookTokenUrl = "https://graph.facebook.com/oauth/access_token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", facebookLoginClientId);
        map.add("redirect_uri", facebookCallbackUrl);
        map.add("client_secret", facebookApiSecretKey);
        map.add("code", code);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(facebookTokenUrl, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            // 액세스 토큰 값을 파싱하여 반환
            String accessToken = null;
            if (responseBody != null && responseBody.contains("access_token")) {
                String[] parts = responseBody.split("&");
                for (String part : parts) {
                    String[] keyValue = part.split("=");
                    if (keyValue.length == 2 && "access_token".equals(keyValue[0])) {
                        accessToken = keyValue[1];
                        break;
                    }
                }
            }
            return accessToken;
        } else {
            log.error("페이스북 인증 코드로 액세스 토큰을 가져오는 중 오류 발생. 응답: {}", response.getBody());
            throw new RuntimeException("페이스북 인증 코드로 액세스 토큰을 가져오는 중 오류 발생.");
        }
    }




}
