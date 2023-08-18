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
import team6.sobun.domain.user.dto.social.KakaoDto;
import team6.sobun.domain.user.entity.Location;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.domain.user.repository.UserRepository;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KakaoService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    @Value("${kakao.login.callback.url}")
    private String kakaoCallbackUrl;

    @Value("${kakao.login.client.id}")
    private String kakaoLoginClientId;



    public User kakaoSignUpOrLinkUser(String code) throws JsonProcessingException {
        log.info("카카오 로그인 시도 중. 인증 코드: {}", code);

        String accessToken = getToken(code);
        log.info("카카오 서버에서 토큰 받기 성공적. 액세스 토큰: {}", accessToken);

        KakaoDto kakaoUserDto = getKakaoUserInfo(accessToken);
        log.info("카카오 사용자 정보: id={}, nickname={}, email={}", kakaoUserDto.getId(), kakaoUserDto.getNickname(), kakaoUserDto.getEmail());

        User user = userRepository.findByEmail(kakaoUserDto.getEmail()).orElse(null);
        if (user == null) {
            log.info("새로운 카카오 사용자 등록을 진행합니다.");
            user = registerKakaoUser(kakaoUserDto);
        } else {
            log.info("기존 사용자와 연결되는 카카오 사용자로 등록합니다.");
        }
        return user;
    }
    private KakaoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        URI uri = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com")
                .path("/v2/user/me")
                .encode()
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        RequestEntity<Void> requestEntity = RequestEntity
                .get(uri)
                .headers(headers)
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        Long id = jsonNode.get("id").asLong();
        String email = jsonNode.get("kakao_account").get("email").asText();
        String phoneNumber = String.valueOf(id);
        String username = jsonNode.get("properties").get("nickname").asText();
        String nickname = "카카오유저"+ id;
        String profileImageUrl = jsonNode.get("properties").get("profile_image").asText();
        log.info("카카오 사용자 정보: " + id + ", " + username + ", " + email);
        return new KakaoDto(id, email, phoneNumber, username, nickname, profileImageUrl); // 프로필 이미지 URL도 KakaoDto에 추가해서 반환
    }
    public String getToken(String code) {
        // Use the secret key from the environment
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("client_id", kakaoLoginClientId);
        map.add("redirect_uri", kakaoCallbackUrl);
        map.add("code", code);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(kakaoAuthUrl, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.get("access_token").asText();
            } catch (JsonProcessingException e) {
                log.error("Kakao API 응답 파싱 오류.", e);
                throw new RuntimeException("Kakao API 응답 파싱 오류.", e);
            }
        } else {
            log.error("Kakao 인증 코드를 액세스 토큰으로 교환하는 중 오류 발생. 응답: {}", response.getBody());
            throw new RuntimeException("Kakao 인증 코드를 액세스 토큰으로 교환하는 중 오류 발생.");
        }
    }
    @Transactional
    public User registerKakaoUser(KakaoDto kakaoUserDto) {
        User user = userRepository.findByEmail(kakaoUserDto.getEmail()).orElse(null);
        if (user != null) {
            user.kakaoIdUpdate(kakaoUserDto);
        } else {
            String randomPwd = passwordEncoder.encode(String.valueOf(kakaoUserDto.getId()));
            String profileImageUrl = kakaoUserDto.getProfileImageUrl(); // 프로필 이미지 URL 가져오기
            user = new User(kakaoUserDto, randomPwd, profileImageUrl); // 프로필 이미지 URL로 User 객체 생성
            userRepository.save(user);
        }
        return user;
    }
}
