package team6.sobun.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import team6.sobun.domain.user.dto.MypageRequestDto;
import team6.sobun.domain.user.dto.SignupRequestDto;
import team6.sobun.domain.user.service.UserService;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.UserDetailsImpl;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원 가입을 처리하는 메소드입니다.
     *
     * @param signupRequestDto 회원 가입 요청 DTO
     * @return 처리 결과에 대한 ApiResponse
     */

    @PostMapping("/auth/signup")
    public ApiResponse<?> signup(@RequestBody @Valid SignupRequestDto signupRequestDto) {
        return userService.signup(signupRequestDto);
    }

    @PatchMapping("/mypage/{id}")
    public ApiResponse<?> nicknameChange(@PathVariable Long id,
                                         @RequestBody MypageRequestDto mypageRequestDto,
                                         @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return userService.nicknameChange(id, mypageRequestDto, userDetailsImpl.getUser());
    }
}


