package team6.sobun.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.domain.user.service.AdminPageService;
import team6.sobun.domain.user.service.UserService;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.responseDto.ErrorResponse;
import team6.sobun.global.security.UserDetailsImpl;
import team6.sobun.global.stringCode.ErrorCodeEnum;

@Tag(name = "관리자 페이지 관련 API", description = "관리자 페이지 관련")
@RestController
@RequiredArgsConstructor
public class AdminPageController {

    private final AdminPageService adminPageService;
    private final UserService userService;

    @Operation(summary = "전체 유저 조회")
    @GetMapping("/admin/userList")
    public ApiResponse<?> userList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails.getUser().getRole() == UserRoleEnum.ADMIN) {
            return adminPageService.userList();
        } else {
            return ApiResponse.error(new ErrorResponse(ErrorCodeEnum.NO_PERMISSIONS));
        }
    }

    @Operation(summary = "권한정지 유저 조회")
    @GetMapping("/admin/blackUser")
    public ApiResponse<?> blackUserList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails.getUser().getRole() == UserRoleEnum.ADMIN) {
            return adminPageService.userBlackList();
        } else {
            return ApiResponse.error(new ErrorResponse(ErrorCodeEnum.NO_PERMISSIONS));
        }
    }
    @Operation(summary = "신고내역 전체 조회")
    @GetMapping("/admin/reportList")
    public ApiResponse<?> searchAllUserReports(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.success( userService.searchAllUserReports(userDetails.getUser()));
    }
    @Operation(summary = "신고내역 상세 조회")
    @GetMapping("/admin/reportList/{reportedUserId}")
    public ApiResponse<?> searchAllUserReportDetail(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @PathVariable Long reportedUserId) {
        return ApiResponse.success( userService.searchUserReportDetail(userDetails.getUser(),reportedUserId));
    }
}
