package team6.sobun.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import team6.sobun.domain.user.service.UserPopularityService;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.UserDetailsImpl;

@Tag(name ="유저 인기도 관련 API", description = "유저가 유저에게 인기도 주거나 다시 뺏기")
@RestController
@RequiredArgsConstructor
public class UserPopularityController {

    private final UserPopularityService userPopularityService;

    @Operation(summary = "인기도 주거나 뺏기")
    @PostMapping("/popularity/{receiverUserId}")
    public ApiResponse<?> popularity(@PathVariable Long receiverUserId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userPopularityService.popularity(receiverUserId, userDetails.getUser());
    }
}
