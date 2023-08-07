package team6.sobun.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import team6.sobun.domain.user.dto.LocationRquestDto;
import team6.sobun.domain.user.service.LocationService;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.UserDetailsImpl;

@RestController
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/auth/location")
    public ApiResponse<?> locationUpdate(@RequestBody LocationRquestDto locationRquestDto,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return locationService.locationUpdate(locationRquestDto, userDetails.getUser());
    }
}
