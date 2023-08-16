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
import team6.sobun.domain.user.dto.social.NaverUserInfo;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.domain.user.repository.UserRepository;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NaverService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    @Value("${naver.login.client.id}")
    private String YOUR_NAVER_LOGIN_CLIENT_ID;

    @Value("${naver.api.secret-key}")
    private String YOUR_NAVER_API_SECRET_KEY;

    public User naverSignUpOrLinkUser(String code) throws JsonProcessingException {
        log.info("네이버 로그인 시도 중. 인증 코드: {}", code);

        String accessToken = getNaverToken(code);
        log.info("네이버 서버에서 토큰 받기 성공적. 액세스 토큰: {}", accessToken);

        NaverUserInfo naverUserInfo = getNaverUserInfo(accessToken);
        log.info("네이버 사용자 정보: id={}, nickname={}, email={}, profileImageUrl={}",
                naverUserInfo.getId(), naverUserInfo.getNickname(), naverUserInfo.getEmail(), naverUserInfo.getProfileImageUrl());

        User user = userRepository.findByEmail(naverUserInfo.getEmail()).orElse(null);
        if (user == null) {
            log.info("새로운 네이버 사용자 등록을 진행합니다.");
            user = registerNaverUser(naverUserInfo);
        } else {
            log.info("기존 사용자와 연결되는 네이버 사용자로 등록합니다.");
            // 네이버 사용자와 기존 사용자를 연결하거나 필요한 정보를 업데이트하는 로직 추가 (예: 프로필 이미지 URL 업데이트 등)
            user.setNickname(naverUserInfo.getNickname());
            user.setProfileImageUrl(naverUserInfo.getProfileImageUrl());
        }
        return user;
    }

    private NaverUserInfo getNaverUserInfo(String accessToken) throws JsonProcessingException {
        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com/v1/nid/me")
                .encode()
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        RequestEntity<Void> requestEntity = RequestEntity
                .get(uri)
                .headers(headers)
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody()).get("response");
                NaverUserInfo naverUserInfo = new NaverUserInfo();
                String id = jsonNode.get("id").asText();
                naverUserInfo.setId(id);
                naverUserInfo.setUsername(jsonNode.get("nickname").asText());
                naverUserInfo.setNickname("네이버유저" + id);
                naverUserInfo.setEmail(jsonNode.get("email").asText());
                naverUserInfo.setProfileImageUrl(jsonNode.get("profile_image").asText());
                naverUserInfo.setLocation("대구시"); // location을 "대구시"로 설정
                return naverUserInfo;
            } catch (JsonProcessingException e) {
                log.error("네이버 API 응답 파싱 오류.", e);
                throw new RuntimeException("네이버 API 응답 파싱 오류.", e);
            }
        } else {
            log.error("네이버 인증 코드로 사용자 정보를 가져오는 중 오류 발생. 응답: {}", response.getBody());
            throw new RuntimeException("네이버 인증 코드로 사용자 정보를 가져오는 중 오류 발생.");
        }
    }


    @Transactional
    public User registerNaverUser(NaverUserInfo naverUserInfo) {
        User user = userRepository.findByEmail(naverUserInfo.getEmail()).orElse(null);
        if (user == null) {
            // 사용자가 존재하지 않으면 새로운 사용자를 생성합니다.
            String randomPwd = passwordEncoder.encode(String.valueOf(naverUserInfo.getId()));
            user = User.builder()
                    .email(naverUserInfo.getEmail())
                    .nickname(naverUserInfo.getNickname())
                    .password(randomPwd)
                    .profileImageUrl(naverUserInfo.getProfileImageUrl())
                    .role(UserRoleEnum.USER)
                    .build();
            userRepository.save(user);
        } else {
            // 사용자가 이미 존재하면 프로필 정보를 업데이트합니다.
            user.setNickname(naverUserInfo.getNickname());
            if (naverUserInfo.getProfileImageUrl() != null) {
                user.setProfileImageUrl(naverUserInfo.getProfileImageUrl());
            }
        }
        return user;
    }
    public String getNaverToken(String code) {
        String naverTokenUrl = "https://nid.naver.com/oauth2.0/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("client_id", YOUR_NAVER_LOGIN_CLIENT_ID);
        map.add("client_secret", YOUR_NAVER_API_SECRET_KEY);
        map.add("code", code);
        map.add("state", "STATE");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(naverTokenUrl, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.get("access_token").asText();
            } catch (JsonProcessingException e) {
                log.error("네이버 API 응답 파싱 오류.", e);
                throw new RuntimeException("네이버 API 응답 파싱 오류.", e);
            }
        } else {
            log.error("네이버 인증 코드로 액세스 토큰을 가져오는 중 오류 발생. 응답: {}", response.getBody());
            throw new RuntimeException("네이버 인증 코드로 액세스 토큰을 가져오는 중 오류 발생.");
        }
    }

}
