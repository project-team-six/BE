package team6.sobun.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import team6.sobun.domain.user.dto.find.FindEmailRequestDto;
import team6.sobun.domain.user.dto.find.FindEmailResponseDto;
import team6.sobun.domain.user.dto.find.PasswordRequestDto;
import team6.sobun.domain.user.service.UserService;
import team6.sobun.domain.user.service.util.MyPageService;
import team6.sobun.global.responseDto.ApiResponse;

@Tag(name = "이메일 찾기 및 비밀번호 찾기", description = "send mail을 이용한 비밀번호 찾기 및 이메일 찾기")
@RestController
@RequiredArgsConstructor
public class UserInfoFindController {

    private final MyPageService myPageService;

    @Operation(summary = "비밀번호 찾기")
    @PostMapping("/auth/findpassword")
    public ApiResponse<?> findPassword(@Valid @RequestBody PasswordRequestDto requestDto) throws Exception {
        return myPageService.findPassword(requestDto);
    }

    @Operation(summary = "이메일 찾기")
    @PostMapping("/auth/findemail")
    public ApiResponse<?> findEmail(@Valid @RequestBody FindEmailRequestDto requestDto) {
        FindEmailResponseDto response = myPageService.findEmail(requestDto);
        return ApiResponse.success(response);
    }
}
