package team6.sobun.domain.user.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.user.dto.SignupRequestDto;
import team6.sobun.domain.user.dto.mypage.MypageRequestDto;
import team6.sobun.domain.user.dto.mypage.MypageResponseDto;
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
    private  final FacebookService facebookService;

    @PostMapping("/signup")
    public ApiResponse<?> signup(@RequestPart(value = "data") SignupRequestDto signupRequestDto,
                                 @RequestPart(value = "file", required = false) MultipartFile image) throws MessagingException {
        return userService.signup(signupRequestDto, image);
    }
    @GetMapping("/email")
    public ApiResponse<?> verifyEmail(@RequestParam String verificationToken) {
        return userService.verifyEmail(verificationToken);
    }
    @PostMapping("/admin/{userId}")
    public ApiResponse<?> makeUserAdmin(@PathVariable Long userId) {
        return userService.makeUserAdmin(userId);
    }
    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestHeader("Authorization") String token,HttpServletResponse response) {
        return userService.logout(token,response);
    }
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
            MypageResponseDto responseDto = myPageService.getUserDetails(userid);
            return success(responseDto);
        }
    }

    @PutMapping("mypage/{userId}")
    public ApiResponse<?> updateUserProfile(@PathVariable Long userId,
                                                    @RequestPart(value = "data", required = false) MypageRequestDto mypageRequestDto,
                                                    @RequestPart(value = "file", required = false) MultipartFile image,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        User user = userDetailsImpl.getUser();
        return myPageService.updateUserProfile(userId, mypageRequestDto, image, user);
    }


    @Transactional
    @PostMapping ("/kakao/login")
    public ApiResponse<?> kakaoCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("카카오 로그인 콜백 요청 받음. 인증 코드: {}", code);

        // 카카오 로그인에 성공한 후, 사용자 정보 가져오기
        User user = kakaoService.kakaoSignUpOrLinkUser(code);
        log.info("카카오 로그인 성공. 유저 ID: {}", user.getId());
        userService.addToken(user,response);
        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);
    }

    @Transactional
    @PostMapping("/google/login")
    public ApiResponse<?> googleCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("구글 로그인 콜백 요청 받음. 인증 코드: {}", code);

        // 구글 로그인에 성공한 후, 사용자 정보 가져오기
        User user = googleService.googleSignUpOrLinkUser(code);
        userService.addToken(user,response);
        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);
    }

    @Transactional
    @PostMapping("/naver/login")
    public ApiResponse<?> naverCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("네이버 로그인 콜백 요청 받음. 인증 코드: {}", code);

        // 네이버 로그인에 성공한 후, 사용자 정보 가져오기
        User user = naverService.naverSignUpOrLinkUser(code);
        log.info("네이버 로그인 성공. 유저 ID: {}", user.getId());
        userService.addToken(user,response);
        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);
    }
    @PostMapping("/facebook/login")
    public ApiResponse<?> facebookCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("페이스북 로그인 콜백 요청 받음. 인증 코드: {}", code);
        // 페이스북 로그인에 성공한 후, 사용자 정보 가져오기
        User user = facebookService.facebookSignUpOrLinkUser(code);
        log.info("페이스북 로그인 성공. 유저 ID: {}", user.getId());
        userService.addToken(user,response);

        return ApiResponse.okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);
    }

}





