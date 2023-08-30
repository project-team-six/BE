package team6.sobun.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import team6.sobun.domain.user.dto.LoginRequestDto;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.jwt.entity.RefreshToken;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.responseDto.ErrorResponse;
import team6.sobun.global.security.UserDetailsImpl;
import team6.sobun.global.security.repository.RefreshTokenRedisRepository;
import team6.sobun.global.stringCode.ErrorCodeEnum;
import team6.sobun.global.stringCode.SuccessCodeEnum;

import java.io.IOException;

import static team6.sobun.global.utils.ResponseUtils.customError;
import static team6.sobun.global.utils.ResponseUtils.okWithMessage;

/**
 * JWT 인증 필터 클래스입니다.
 * 사용자의 로그인 요청을 인증하고 JWT를 생성하여 응답에 추가합니다.
 */
@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRedisRepository redisRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * JwtAuthenticationFilter 생성자입니다.
     *
     * @param jwtProvider     JwtProvider 인스턴스
     * @param redisRepository RefreshTokenRedisRepository 인스턴스
     */
    public JwtAuthenticationFilter(JwtProvider jwtProvider, RefreshTokenRedisRepository redisRepository, UserRepository userRepository,PasswordEncoder passwordEncoder) {
        this.jwtProvider = jwtProvider;
        this.redisRepository = redisRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        setFilterProcessesUrl("/auth/login");
    }

    /**
     * 로그인 시도를 인증합니다.
     *
     * @param request  HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @return Authentication 객체
     * @throws AuthenticationException 인증 예외가 발생한 경우
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("로그인 시도");

        try {
            LoginRequestDto loginRequestDto = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);

            UserDetailsImpl userDetails = loadUserByUsername(loginRequestDto.getEmail());

            if (userDetails == null) {
                sendErrorResponse(response, "이메일이 올바르지 않습니다.", HttpServletResponse.SC_UNAUTHORIZED);
                return null;
            }

            if (userDetails.getUser().getRole() == UserRoleEnum.BLACK) {
                sendErrorResponse(response, "관리자에 의해 정지된 계정입니다.", HttpServletResponse.SC_UNAUTHORIZED);
                return null;
            }


            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getEmail(),
                            loginRequestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error("로그인 시도 중 예외 발생: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String errorMessage, int statusCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(false)
                .error(new ErrorResponse(errorMessage, statusCode))
                .build();

        String jsonResponse;
        try {
            jsonResponse = new ObjectMapper().writeValueAsString(apiResponse);
            response.getWriter().write(jsonResponse);
        } catch (IOException ex) {
            log.error("에러 메시지 JSON 변환 중 예외 발생: {}", ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }


    private boolean isPasswordValid(String password, String encodedPassword) {

        return passwordEncoder.matches(password, encodedPassword);
    }

    private UserDetailsImpl loadUserByUsername(String email) {

        return userRepository.findByEmail(email)
                .map(UserDetailsImpl::new)
                .orElse(null);
    }


    /**
     * 로그인 성공 시 JWT를 생성하여 응답에 추가합니다.
     *
     * @param request    HttpServletRequest 객체
     * @param response   HttpServletResponse 객체
     * @param chain      FilterChain 객체
     * @param authResult 인증 결과 Authentication 객체
     * @throws IOException      입출력 예외가 발생한 경우
     * @throws ServletException Servlet 예외가 발생한 경우
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.info("로그인 성공 및 JWT 생성");
        ObjectMapper objectMapper = new ObjectMapper();

        // 사용자 정보 가져오기
        String username = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
        String nickname = ((UserDetailsImpl) authResult.getPrincipal()).getNickname();
        Long userId = ((UserDetailsImpl) authResult.getPrincipal()).getUserId();
        UserRoleEnum role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getRole();
        String profileImageUrl = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getProfileImageUrl() ;
        String location = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getLocation() == null ? "서울특별시 강남구 역삼동" : ((UserDetailsImpl) authResult.getPrincipal()).getUser().getLocation().myAddress(
                ((UserDetailsImpl) authResult.getPrincipal()).getUser().getLocation().getSido(),
                ((UserDetailsImpl) authResult.getPrincipal()).getUser().getLocation().getSigungu(),
                ((UserDetailsImpl) authResult.getPrincipal()).getUser().getLocation().getDong());

        // 카카오 로그인의 경우 username에 카카오 이메일 정보가 담겨있을 것이므로 해당 값을 그대로 사용

        String token = jwtProvider.createToken(String.valueOf(userId),username, nickname, role, profileImageUrl, location);
        String refreshToken = jwtProvider.createRefreshToken(String.valueOf(userId),username, nickname, role, profileImageUrl, location);
        jwtProvider.addJwtHeaders(token,refreshToken, response);


        // refresh 토큰은 redis에 저장
        RefreshToken refresh = RefreshToken.builder()
                .id(username)
                .refreshToken(refreshToken)
                .build();
        redisRepository.save(refresh);

        ApiResponse<?> apiResponse = okWithMessage(SuccessCodeEnum.USER_LOGIN_SUCCESS);

        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
        response.setStatus(HttpServletResponse.SC_OK);
    }


    /**
     * 로그인 실패 시 실패 응답을 반환합니다.
     *
     * @param request  HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @param failed   AuthenticationException 객체
     * @throws IOException      입출력 예외가 발생한 경우
     * @throws ServletException Servlet 예외가 발생한 경우
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("로그인 실패");
        ObjectMapper objectMapper = new ObjectMapper();

        // 로그인 실패 응답 반환
        ApiResponse<?> apiResponse = customError(ErrorCodeEnum.LOGIN_FAIL);
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

}
