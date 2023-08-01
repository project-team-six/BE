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
import team6.sobun.domain.user.dto.social.GoogleUserInfo;
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
public class GoogleService {

    private final UserRepository userRepository;
    private final IdenticonService identiconService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRedisRepository redisRepository;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;
    private final S3Service s3Service;

    @Value("${google.login.callback.url}")
    private String googleCallbackUrl;

    @Value("${google.login.client.id}")
    private String googleLoginClientId;

    @Value("${google.api.secret-key}")
    private String googleApiSecretKey;

    private static final String GOOGLE_USER_NICKNAME_PREFIX = "구글유저";


    public User googleSignUpOrLinkUser (String code) throws JsonProcessingException {
        log.info("구글 로그인 시도 중. 인증 코드: {}", code);

        String accessToken = getGoogleToken(code);
        log.info("구글 서버에서 토큰 받기 성공적. 액세스 토큰: {}", accessToken);

        GoogleUserInfo googleUserInfo = getGoogleUserInfo(accessToken);
        log.info("구글 사용자 정보: id={}, nickname={}, email={}, profileImageUrl={}",
                googleUserInfo.getSub(), googleUserInfo.getName(), googleUserInfo.getEmail(), googleUserInfo.getPicture());

        User user = userRepository.findByEmail(googleUserInfo.getEmail()).orElse(null);
        if (user == null) {
            log.info("새로운 구글 사용자 등록을 진행합니다.");
            user = registerGoogleUser(googleUserInfo);
        } else {
            log.info("기존 사용자와 연결되는 구글 사용자로 등록합니다.");
            // 구글 사용자와 기존 사용자를 연결하거나 필요한 정보를 업데이트하는 로직 추가 (예: 프로필 이미지 URL 업데이트 등)
            user.setNickname(googleUserInfo.getName());
            user.setProfileImageUrl(googleUserInfo.getPicture());
        }
        return user;
    }

    private GoogleUserInfo getGoogleUserInfo(String accessToken) throws JsonProcessingException {
        URI uri = UriComponentsBuilder
                .fromUriString("https://www.googleapis.com/oauth2/v3/userinfo")
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
                return objectMapper.readValue(response.getBody(), GoogleUserInfo.class);
            } catch (JsonProcessingException e) {
                log.error("구글 API 응답 파싱 오류.", e);
                throw new RuntimeException("구글 API 응답 파싱 오류.", e);
            }
        } else {
            log.error("구글 인증 코드로 사용자 정보를 가져오는 중 오류 발생. 응답: {}", response.getBody());
            throw new RuntimeException("구글 인증 코드로 사용자 정보를 가져오는 중 오류 발생.");
        }
    }
    @Transactional
    public User registerGoogleUser(GoogleUserInfo googleUserInfo) {
        User user = userRepository.findByEmail(googleUserInfo.getEmail()).orElse(null);
        if (user == null) {
            // 사용자가 존재하지 않으면 새로운 사용자를 생성합니다.
            String randomPwd = passwordEncoder.encode(String.valueOf(googleUserInfo.getSub()));
            String username = googleUserInfo.getName();
            String nickname = GOOGLE_USER_NICKNAME_PREFIX + googleUserInfo.getSub(); // Create a default nickname
            String location = "대구시"; // Set the default location
            user = User.builder()
                    .email(googleUserInfo.getEmail())
                    .nickname(nickname)
                    .location(location)
                    .password(randomPwd)
                    .profileImageUrl(googleUserInfo.getPicture())
                    .role(UserRoleEnum.USER)
                    .username(username)
                    .build();
            userRepository.save(user);

        } return user;
    }

    public String getGoogleToken (String code){
        // 구글 인증 서버에 인증 코드를 전달하여 액세스 토큰을 받아온다.
        String googleTokenUrl = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("code", code);
        map.add("client_id", googleLoginClientId);
        map.add("client_secret", googleApiSecretKey);
        map.add("redirect_uri", googleCallbackUrl);
        map.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(googleTokenUrl, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.get("access_token").asText();
            } catch (JsonProcessingException e) {
                log.error("구글 API 응답 파싱 오류.", e);
                throw new RuntimeException("구글 API 응답 파싱 오류.", e);
            }
        } else {
            log.error("구글 인증 코드로 액세스 토큰을 가져오는 중 오류 발생. 응답: {}", response.getBody());
            throw new RuntimeException("구글 인증 코드로 액세스 토큰을 가져오는 중 오류 발생.");
        }
    }



}
