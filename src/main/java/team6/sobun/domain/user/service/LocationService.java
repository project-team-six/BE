package team6.sobun.domain.user.service;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.sobun.domain.user.dto.LocationRquestDto;
import team6.sobun.domain.user.entity.Location;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.LocationRepository;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.jwt.entity.RefreshToken;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.repository.RefreshTokenRedisRepository;
import team6.sobun.global.stringCode.SuccessCodeEnum;
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final RefreshTokenRedisRepository redisRepository;
    private final EntityManager em;
    private final JwtProvider jwtProvider;

    @Transactional
    public ApiResponse<?> locationUpdate(HttpServletResponse response,LocationRquestDto locationRquestDto, User user) {
        User findUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 다릅니다."));

        Location checkLocation = findUser.getLocation();
        if (checkLocation != null) {
            checkLocation.update(locationRquestDto);
            checkLocation = em.merge(checkLocation);
            locationRepository.save(checkLocation);
            String token = jwtProvider.createToken(String.valueOf(user.getId()),user.getEmail(), user.getNickname(), user.getRole(),
                    user.getProfileImageUrl(), user.getLocation().myAddress(user.getLocation().getSido(), user.getLocation().getSigungu(), user.getLocation().getDong()));
            String refreshToken = jwtProvider.createRefreshToken(String.valueOf(user.getId()),user.getEmail(), user.getNickname(), user.getRole(),
                    user.getProfileImageUrl(), user.getLocation().myAddress(user.getLocation().getSido(), user.getLocation().getSigungu(), user.getLocation().getDong()));
            jwtProvider.addJwtHeaders(token,refreshToken, response);

            // refresh 토큰은 redis에 저장
            RefreshToken refresh = RefreshToken.builder()
                    .id(user.getEmail())
                    .refreshToken(refreshToken)
                    .build();
            log.info("리프레쉬 토큰 저장 성공. 유저 ID: {}", user.getId());
            redisRepository.save(refresh);
            return ApiResponse.okWithMessage(SuccessCodeEnum.LOCATION_CHANGE_SUCCESS);
        }

        findUser.updateLocation(locationRquestDto, user);
        userRepository.save(findUser);
        locationRepository.save(findUser.getLocation());
        String token = jwtProvider.createToken(String.valueOf(user.getId()),user.getEmail(), user.getNickname(), user.getRole(),
                user.getProfileImageUrl(), user.getLocation().myAddress(user.getLocation().getSido(), user.getLocation().getSigungu(), user.getLocation().getDong()));
        String refreshToken = jwtProvider.createRefreshToken(String.valueOf(user.getId()),user.getEmail(), user.getNickname(), user.getRole(),
                user.getProfileImageUrl(), user.getLocation().myAddress(user.getLocation().getSido(), user.getLocation().getSigungu(), user.getLocation().getDong()));
        jwtProvider.addJwtHeaders(token,refreshToken, response);

        // refresh 토큰은 redis에 저장
        RefreshToken refresh = RefreshToken.builder()
                .id(user.getEmail())
                .refreshToken(refreshToken)
                .build();
        log.info("리프레쉬 토큰 저장 성공. 유저 ID: {}", user.getId());
        redisRepository.save(refresh);
        return ApiResponse.okWithMessage(SuccessCodeEnum.LOCATION_CHANGE_SUCCESS);
    }
}

