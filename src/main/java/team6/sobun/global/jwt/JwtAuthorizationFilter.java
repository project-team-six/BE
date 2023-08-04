package team6.sobun.global.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import team6.sobun.global.security.UserDetailsServiceImpl;
import team6.sobun.global.security.repository.RefreshTokenRedisRepository;

import java.io.IOException;

@Slf4j(topic = "JWT 검증, 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final RefreshTokenRedisRepository redisRepository;

    /**
     * 요청을 필터링하여 JWT 인증 및 인가를 처리합니다.
     *
     * @param req   HttpServletRequest 객체
     * @param res   HttpServletResponse 객체
     * @param chain FilterChain 객체
     * @throws ServletException 서블릿 예외가 발생한 경우
     * @throws IOException      입출력 예외가 발생한 경우
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        // 클라이언트에서 헤더로 넘어온 토큰을 추출합니다.
        String token = jwtProvider.getTokenFromHeader(req);
        log.info("헤더에서 토큰 정보를 가져옵니다.");

        if (StringUtils.hasText(token)) {
            // 액세스 토큰에서 유저 정보를 추출합니다.
            log.info("엑세스 토큰에서 정보를 추출합니다.");
            token = jwtProvider.substringHeaderToken(token);
            log.info("토큰 정보를 가져옵니다.");

            Claims info = null;

            if (!jwtProvider.validateToken(token)) {
                // 액세스 토큰이 유효하지 않은 경우
                log.error("유효하지 않은 액세스 토큰입니다.");

                String refreshToken = jwtProvider.getTokenFromHeader(req);
                jwtProvider.handleExpiredAccessToken(req, res);

            } else {
                // 유효한 액세스 토큰일 경우, 해당 사용자로 인증 정보를 설정합니다.
                try {
                    info = jwtProvider.getUserInfoFromToken(token);
                    setAuthentication(info.getSubject());
                } catch (Exception e) {
                    log.error("인증 정보 설정 중 예외 발생: {}", e.getMessage());
                    return;
                }
            }
        }

        // 다음 필터로 요청을 전달합니다.
        chain.doFilter(req, res);
    }



    /**
     * 주어진 사용자 이름을 기반으로 인증을 설정합니다.
     *
     * @param username 사용자 이름
     */
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    /**
     * 인증 객체를 생성합니다.
     *
     * @param username 사용자 이름
     * @return 인증 객체
     */
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
