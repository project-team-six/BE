package team6.sobun.domain.user.service;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import team6.sobun.domain.pin.repository.PinRepository;
import team6.sobun.domain.post.entity.Post;
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.domain.user.dto.*;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.exception.InvalidConditionException;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.repository.RefreshTokenRedisRepository;
import team6.sobun.global.stringCode.ErrorCodeEnum;
import team6.sobun.global.stringCode.SuccessCodeEnum;
import team6.sobun.global.utils.ResponseUtils;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final IdenticonService identiconService; // IdenticonService 주입
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRedisRepository redisRepository;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;
    private final S3Service s3Service;
    private final PinRepository pinRepository;


    @Value("${kakao.login.callback.url}")
    private String kakaoCallbackUrl;

    @Value("${kakao.login.client.id}")
    private String kakaoLoginClientId;

    @Value("${kakao.api.secret-key}")
    private String kakaoApiSecretKey;

    @Value("${naver.login.client.id}")
    private String YOUR_NAVER_LOGIN_CLIENT_ID;

    @Value("${naver.api.secret-key}")
    private String YOUR_NAVER_API_SECRET_KEY;

    @Value("${facebook.login.callback.url}")
    private String facebookCallbackUrl;

    @Value("${facebook.login.client.id}")
    private String facebookLoginClientId;

    @Value("${facebook.api.secret-key}")
    private String facebookApiSecretKey;




    public ApiResponse<?> signup(SignupRequestDto signupRequestDto, MultipartFile image) {
        String email = signupRequestDto.getEmail();
        String nickname = signupRequestDto.getNickname();
        String location = signupRequestDto.getLocation();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());

        checkDuplicatedEmail(email);
        UserRoleEnum role = UserRoleEnum.USER;


        String profileImageUrl = null;
        if (image != null && !image.isEmpty()) {
            // 이미지가 있을 경우에만 S3에 업로드하고 URL을 가져옴
            profileImageUrl = s3Service.upload(image);
        }

        // 프로필 이미지 URL을 사용하여 User 객체 생성
        User user = new User(email, location, nickname, password, role);
        userRepository.save(user);

        log.info("'{}' 이메일을 가진 사용자가 가입했습니다.", email);

        return ResponseUtils.okWithMessage(SuccessCodeEnum.USER_SIGNUP_SUCCESS);
    }


    public ApiResponse<?> withdraw(User user) {
        userRepository.delete(user);
        redisRepository.deleteById(user.getNickname());

        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_WITHRAW_SUCCESS);
    }

    public ApiResponse<?> logout(User user) {
        redisRepository.deleteById(user.getNickname());

        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGOUT_SUCCESS);
    }

    @Transactional
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
        String location = "대구시";
        String username = jsonNode.get("properties").get("nickname").asText();
        String nickname = "카카오유저"+ id;
        String profileImageUrl = jsonNode.get("properties").get("profile_image").asText(); // 프로필 이미지 URL 가져오기
        log.info("카카오 사용자 정보: " + id + ", " + username + ", " + email);
        return new KakaoDto(id, email,location, username, nickname, profileImageUrl); // 프로필 이미지 URL도 KakaoDto에 추가해서 반환
    }

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
                naverUserInfo.setId(jsonNode.get("id").asText());
                naverUserInfo.setNickname(jsonNode.get("nickname").asText());
                naverUserInfo.setEmail(jsonNode.get("email").asText());
                naverUserInfo.setProfileImageUrl(jsonNode.get("profile_image").asText());
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
        map.add("client_secret", kakaoApiSecretKey); // Add the secret key to the request
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



    private void checkDuplicatedEmail(String email) {
        Optional<User> found = userRepository.findByEmail(email);
        if (found.isPresent()) {
            throw new InvalidConditionException(ErrorCodeEnum.DUPLICATE_USERNAME_EXIST);
        }
    }


    @Transactional
    public ApiResponse<?> nicknameChange(Long id, MypageRequestDto mypageRequestDto, User user) {
        log.info("닉네임 변경 들어옴");
        User checkUser = userRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 사용자 입니다."));

        if (!checkUser.getId().equals(user.getId())) {
            throw new IllegalArgumentException("동일한 사용자가 아닙니다.");
        }

        checkUser.update(mypageRequestDto);
        return ResponseUtils.okWithMessage(SuccessCodeEnum.USER_NICKNAME_SUCCESS);
    }

    public UserResponseDto getUserInfoWithPinnedPosts(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            // 유저가 존재하지 않을 때 처리
            return null;
        }

        List<Post> pinnedPosts = pinRepository.findByUser_Id(userId);
        // 필요에 따라 pinnedPosts를 정렬하거나 다른 로직을 추가할 수 있습니다.

        UserResponseDto userResponseDto = new UserResponseDto(user);
        userResponseDto.setPinnedPosts(pinnedPosts);

        return userResponseDto;
    }
    public ApiResponse<?> getUserDetails(Long userId) {
        // 사용자를 userId로 조회합니다.
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // 조회한 정보를 DTO로 변환하여 리턴합니다.
            UserDetailResponseDto responseDto = new UserDetailResponseDto(
                    user.getNickname(),
                    user.getProfileImageUrl(),
                    user.getMannerTemperature()
            );
            return ApiResponse.success(responseDto);
        } else {
            // 사용자를 찾지 못한 경우 에러 응답을 리턴합니다.
            throw new IllegalArgumentException();
        }
    }
}







