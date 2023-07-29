package team6.sobun.domain.user.kakao.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import team6.sobun.domain.user.repository.KakaoRepository;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final KakaoRepository kakaoRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //(4)
        String jwtHeader = request.getHeader(JwtProperties.HEADER_STRING);

        //(5)
        if (jwtHeader == null || !jwtHeader.startsWith(JwtProperties.TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        //(6)
        String token = jwtHeader.replace(JwtProperties.TOKEN_PREFIX, "");

        Long userCode = null;

        try {
            //(7)
            Claims claims = Jwts.parser()
                    .setSigningKey(JwtProperties.SECRET.getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            userCode = Long.parseLong(claims.get("id").toString());
        } catch (ExpiredJwtException e) {
            e.printStackTrace();
            request.setAttribute(JwtProperties.HEADER_STRING, "토큰이 만료되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(JwtProperties.HEADER_STRING, "유효하지 않은 토큰입니다.");
        }

        //(8)
        request.setAttribute("userCode", userCode);

        //(9)
        filterChain.doFilter(request, response);
    }
}
