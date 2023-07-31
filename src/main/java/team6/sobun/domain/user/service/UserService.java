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
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.domain.user.dto.KakaoDto;
import team6.sobun.domain.user.dto.MypageRequestDto;
import team6.sobun.domain.user.dto.SignupRequestDto;
import team6.sobun.domain.user.entity.GoogleUserInfo;
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


    @Value("${kakao.login.callback.url}")
    private String kakaoCallbackUrl;

    @Value("${kakao.login.client.id}")
    private String kakaoLoginClientId;

    @Value("${kakao.api.secret-key}")
    private String kakaoApiSecretKey;

    @Value("${google.login.callback.url}")
    private String googleCallbackUrl;

    @Value("${google.login.client.id}")
    private String googleLoginClientId;

    @Value("${google.api.secret-key}")
    private String googleApiSecretKey;



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
        String nickname = jsonNode.get("properties").get("nickname").asText();
        String profileImageUrl = jsonNode.get("properties").get("profile_image").asText(); // 프로필 이미지 URL 가져오기
        log.info("카카오 사용자 정보: " + id + ", " + nickname + ", " + email);
        return new KakaoDto(id, nickname, email, profileImageUrl); // 프로필 이미지 URL도 KakaoDto에 추가해서 반환
    }

    public User googleSignUpOrLinkUser(String code) throws JsonProcessingException {
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
            user = User.builder()
                    .email(googleUserInfo.getEmail())
                    .nickname(googleUserInfo.getName())
                    .password(randomPwd)
                    .profileImageUrl(googleUserInfo.getPicture())
                    .role(UserRoleEnum.USER)
                    .build();
            userRepository.save(user);
        } else {
            // 사용자가 이미 존재하면 프로필 정보를 업데이트합니다.
            user.setNickname(googleUserInfo.getName());
            if (googleUserInfo.getPicture() != null) {
                user.setProfileImageUrl(googleUserInfo.getPicture());
            }
        }
        return user;
    }
    public String getGoogleToken(String code) {
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
}
