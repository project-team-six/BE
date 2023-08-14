package team6.sobun.domain.user.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team6.sobun.domain.user.dto.LocationRquestDto;
import team6.sobun.domain.user.entity.Location;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.LocationRepository;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.stringCode.SuccessCodeEnum;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final EntityManager em;

    @Transactional
    public ApiResponse<?> locationUpdate(LocationRquestDto locationRquestDto, User user) {
        User findUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 다릅니다."));

        Location checkLocation = findUser.getLocation();
        if (checkLocation != null) {
            checkLocation.update(locationRquestDto);
            checkLocation = em.merge(checkLocation);
            locationRepository.save(checkLocation);
            return ApiResponse.okWithMessage(SuccessCodeEnum.LOCATION_CHANGE_SUCCESS);
        }

        findUser.updateLocation(locationRquestDto, user);
        userRepository.save(findUser);
        locationRepository.save(findUser.getLocation());
        return ApiResponse.okWithMessage(SuccessCodeEnum.LOCATION_CHANGE_SUCCESS);
    }
}