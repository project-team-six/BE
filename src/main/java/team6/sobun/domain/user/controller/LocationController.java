package team6.sobun.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import team6.sobun.domain.user.dto.location.LocationRquestDto;
import team6.sobun.domain.user.service.util.LocationService;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.UserDetailsImpl;
import team6.sobun.global.security.repository.RefreshTokenRedisRepository;
@Tag(name = "위치정보 API", description = "위치 정보 등록 및 수정")
@Slf4j
@RestController
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "위치정보 저장 및 수정")
    @PutMapping("/auth/location")
    public ApiResponse<?> locationUpdate(@Valid @RequestBody LocationRquestDto locationRquestDto,
                                         HttpServletResponse response,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return locationService.locationUpdate(locationRquestDto, userDetails.getUser(),response);
    }

}
