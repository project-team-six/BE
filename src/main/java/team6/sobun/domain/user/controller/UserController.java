package team6.sobun.domain.user.controller;


import jakarta.mail.MessagingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.user.dto.AuthEmailInputRequestDto;
import team6.sobun.domain.user.dto.AuthEmailRequestDto;
import team6.sobun.domain.user.dto.SignupRequestDto;
import team6.sobun.domain.user.dto.mypage.MypageRequestDto;
import team6.sobun.domain.user.dto.mypage.MypageResponseDto;
import team6.sobun.domain.user.dto.password.PasswordRequestDto;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.service.UserService;
import team6.sobun.domain.user.service.social.FacebookService;
import team6.sobun.domain.user.service.social.GoogleService;
import team6.sobun.domain.user.service.social.KakaoService;
import team6.sobun.domain.user.service.social.NaverService;
import team6.sobun.domain.user.service.util.MyPageService;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.UserDetailsImpl;
import team6.sobun.global.stringCode.SuccessCodeEnum;

import java.io.IOException;

import static team6.sobun.global.responseDto.ApiResponse.success;

@Tag(name = "유저정보 관련 API", description = "회원가입 및 유저 정보 수정 관련")
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MyPageService myPageService;
    private final KakaoService kakaoService;
    private final GoogleService googleService;
    private final NaverService naverService;
    private final FacebookService facebookService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ApiResponse<?> signup(@Valid @RequestPart(value = "data") SignupRequestDto signupRequestDto,
                                 @RequestPart(value = "file", required = false) MultipartFile image) throws MessagingException {
        return userService.signup(signupRequestDto, image);
    }

    @Operation(summary = "이메일 인증번호 생성")
    @GetMapping("/email")
    public ApiResponse<?> authEmail(@Valid @RequestBody AuthEmailRequestDto authEmailRequestDto) throws MessagingException {
        return userService.authEmail(authEmailRequestDto);
    }

    @Operation(summary = "이메일 인증 확인")
    @GetMapping("/authEmail")
    public ApiResponse<?> authEmailInput(@Valid @RequestBody AuthEmailInputRequestDto authEmailInputRequestDto) {
        return userService.authEmailInput(authEmailInputRequestDto.getEmail(), authEmailInputRequestDto.getAuthNumber());
    }

//    @GetMapping("/email")
//    public ApiResponse<?> verifyEmail(@RequestParam String verificationToken) {
//        return userService.verifyEmail(verificationToken);
//    }

    @Operation(summary = "관리자 권한 부여")
    @PostMapping("/admin/{userId}")
    public ApiResponse<?> makeUserAdmin(@PathVariable Long userId) {
        return userService.makeUserAdmin(userId);
    }

    @Operation(summary = "유저 권한 정지")
    @PostMapping("/black/{userId}")
    public ApiResponse<?> makeUserBlack(@PathVariable Long userId) {
        return userService.makeUserBlack(userId);
    }

    @Operation(summary = "BLACK 유저 -> USER 유저로 변경")
    @PostMapping("/user/{userId}")
    public ApiResponse<?> makeUser(@PathVariable Long userId) {
        return userService.makeUser(userId);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestHeader("Authorization") String token, HttpServletResponse response) {
        return userService.logout(token, response);
    }

    @Operation(summary = "마이페이지 상세 조회")
    @GetMapping("/mypage/{userid}")
    public ApiResponse<?> userDetailView(@PathVariable Long userid, HttpServletRequest req,
                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long requestingUserId = userDetails != null ? userDetails.getUser().getId() : null;
        if (requestingUserId != null && requestingUserId.equals(userid)) {
            // 로그인한 사용자와 조회 대상 사용자가 같은 경우 (본인 페이지 조회)
            log.info("사용자 ID '{}'가 본인 페이지를 조회합니다.", userid);
            MypageResponseDto responseDto = myPageService.getCurrentUserDetails(requestingUserId);
            return success(responseDto);
        } else {
            // 로그인한 사용자와 조회 대상 사용자가 다른 경우 (일반 사용자 페이지 조회)
            log.info("사용자 ID '{}'가 다른 사용자 페이지를 조회합니다.", userid);
            MypageResponseDto responseDto = myPageService.getUserDetails(userid, userDetails.getUser());
            return success(responseDto);
        }
    }

    @Operation(summary = "마이페이지 정보수정")
    @PutMapping("mypage/{userId}")
    public ApiResponse<?> updateUserProfile(@PathVariable Long userId,
                                            @RequestBody MypageRequestDto mypageRequestDto,
                                            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                                            HttpServletResponse response) {
        return myPageService.updateUserProfile(userId, mypageRequestDto, userDetailsImpl.getUser(), response);
    }

    @Operation(summary = "마이페이지 비밀번호 수정")
    @PutMapping("mypagePassword/{userId}")
    public ApiResponse<?> updateUserPassword(@PathVariable Long userId,
                                             @RequestBody PasswordRequestDto passwordRequestDto,
                                             @AuthenticationPrincipal UserDetailsImpl userDetailsImpl
    ) {
        return myPageService.updateUserPassword(userId, passwordRequestDto, userDetailsImpl.getUser());
    }


    @Operation(summary = "마이페이지 프로필 이미지 수정")
    @PutMapping("mypageImage/{userId}")
    public ApiResponse<?> updateUserProfileImage(@PathVariable Long userId,
                                                 @RequestPart(value = "file", required = false) MultipartFile image,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                                                 HttpServletResponse response) {
        User user = userDetailsImpl.getUser();
        return myPageService.updateUserProfileImage(userId, image, user, response);
    }

    @Operation(summary = "카카오 로그인")
    @Transactional
    @PostMapping("/kakao/login")
    public ApiResponse<?> kakaoCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("카카오 로그인 콜백 요청 받음. 인증 코드: {}", code);

        // 카카오 로그인에 성공한 후, 사용자 정보 가져오기
        User user = kakaoService.kakaoSignUpOrLinkUser(code);
        log.info("카카오 로그인 성공. 유저 ID: {}", user.getId());
        userService.addToken(user, response);
        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);
    }

    @Operation(summary = "구글 로그인")
    @Transactional
    @PostMapping("/google/login")
    public ApiResponse<?> googleCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("구글 로그인 콜백 요청 받음. 인증 코드: {}", code);

        // 구글 로그인에 성공한 후, 사용자 정보 가져오기
        User user = googleService.googleSignUpOrLinkUser(code);
        userService.addToken(user, response);
        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);
    }

    @Operation(summary = "네이버 로그인")
    @Transactional
    @PostMapping("/naver/login")
    public ApiResponse<?> naverCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("네이버 로그인 콜백 요청 받음. 인증 코드: {}", code);

        // 네이버 로그인에 성공한 후, 사용자 정보 가져오기
        User user = naverService.naverSignUpOrLinkUser(code);
        log.info("네이버 로그인 성공. 유저 ID: {}", user.getId());
        userService.addToken(user, response);
        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);
    }

    @Operation(summary = "페이스북 로그인")
    @PostMapping("/facebook/login")
    public ApiResponse<?> facebookCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("페이스북 로그인 콜백 요청 받음. 인증 코드: {}", code);
        // 페이스북 로그인에 성공한 후, 사용자 정보 가져오기
        User user = facebookService.facebookSignUpOrLinkUser(code);
        log.info("페이스북 로그인 성공. 유저 ID: {}", user.getId());
        userService.addToken(user, response);

        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);
    }
}





