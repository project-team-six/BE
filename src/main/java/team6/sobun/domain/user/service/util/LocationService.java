package team6.sobun.domain.user.service.util;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.sobun.domain.user.dto.location.LocationRquestDto;
import team6.sobun.domain.user.entity.Location;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.LocationRepository;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.domain.user.service.UserService;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.repository.RefreshTokenRedisRepository;
import team6.sobun.global.stringCode.SuccessCodeEnum;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final LocationRepository locationRepository;
    private final EntityManager em;

  
    @Transactional
    public ApiResponse<?> locationUpdate(LocationRquestDto locationRquestDto, User user,HttpServletResponse response) {
        User findUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 다릅니다."));

        Location checkLocation = findUser.getLocation();
        if (checkLocation != null) {
            checkLocation.update(locationRquestDto);
            checkLocation = em.merge(checkLocation);
            locationRepository.save(checkLocation);
            User updateUser = checkLocation.getUser();
            userService.addToken(updateUser,response);
            return ApiResponse.okWithMessage(SuccessCodeEnum.LOCATION_CHANGE_SUCCESS);
        }

        findUser.updateLocation(locationRquestDto, user);
        userRepository.save(findUser);
        locationRepository.save(findUser.getLocation());
        userService.addToken(findUser,response);
        return ApiResponse.okWithMessage(SuccessCodeEnum.LOCATION_CHANGE_SUCCESS);
    }
  

}

