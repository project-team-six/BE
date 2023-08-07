package team6.sobun.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.global.jwt.entity.RefreshToken;
import team6.sobun.global.security.repository.RefreshTokenRedisRepository;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_KEY = "auth";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 14 * 24 * 60 * 60L;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 14 * 24 * 60 * 60 * 1000L; // 2주

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;


    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    /**
     * 헤더에서 토큰을 추출합니다.
     *
     * @param token 토큰 문자열
     * @return 추출된 토큰
     * @throws NullPointerException 유효한 토큰이 아닌 경우 발생하는 예외
     */
    public String substringHeaderToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            return token.substring(7);
        }
        throw new NullPointerException("유효한 토큰이 아닙니다.");
    }

    /**
     * 헤더에 JWT 토큰을 추가합니다.
     *
     * @param token    JWT 토큰
     * @param response HttpServletResponse 객체
     */
    public void addJwtHeader(String token, HttpServletResponse response) {
        try {
            token = URLEncoder.encode(token, "utf-8").replaceAll("\\+", "%20");
            response.setHeader(AUTHORIZATION_HEADER, token);
        } catch (UnsupportedEncodingException e) {
            log.info(e.getMessage());
        }
    }

    /**
     * 헤더에서 토큰을 가져옵니다.
     *
     * @param req HttpServletRequest 객체
     * @return 헤더에서 추출된 토큰
     */
    public String getTokenFromHeader(HttpServletRequest req) {
        String token = req.getHeader(AUTHORIZATION_HEADER);
        if (token != null) {
            try {
                return URLDecoder.decode(token, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.info(e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * 토큰을 생성합니다.
     *
     * @param username 사용자 이름
     * @param role     사용자 역할
     * @return 생성된 JWT 토큰
     */
    public String createToken(String userId, String username, String nickname, UserRoleEnum role) {
        Date date = new Date();
        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username)
                        .claim("userId", userId)
                        .claim("nickname", nickname)
                        .claim(AUTHORIZATION_KEY, role)
                        .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_EXPIRE_TIME)) // 만료시간
                        .setIssuedAt(date)
                        .signWith(key, signatureAlgorithm)
                        .compact();
    }

    public String createRefreshToken(String userId, String username, String nickname, UserRoleEnum role) {
        Date date = new Date();
        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username)
                        .claim("userId", userId)
                        .claim("nickname", nickname)
                        .claim(AUTHORIZATION_KEY, role)
                        .setExpiration(new Date(date.getTime() + REFRESH_TOKEN_EXPIRE_TIME)) // 만료시간
                        .setIssuedAt(date)
                        .signWith(key, signatureAlgorithm)
                        .compact();
    }

    /**
     * 토큰의 유효성을 검사합니다.
     *
     * @param token 검사할 JWT 토큰
     * @return 토큰의 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }


    /**
     * 토큰에서 사용자 정보를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 추출된 사용자 정보 (Claims 객체)
     */
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public UserRoleEnum getUserRoleEnumFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return UserRoleEnum.valueOf(claims.get(JwtProvider.AUTHORIZATION_KEY, String.class));
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 생성합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰
     */
    public String createAccessTokenFromRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(refreshToken).getBody();
            String id = claims.getSubject();
            UserRoleEnum role = UserRoleEnum.valueOf(claims.get(AUTHORIZATION_KEY, String.class));
            Date date = new Date();
            return BEARER_PREFIX +
                    Jwts.builder()
                            .setSubject(id) // 사용자 식별
                            .claim(AUTHORIZATION_KEY, role)
                            .setExpiration(new Date(date.getTime() + 14 * 24 * 60 * 60)) // 만료 시간
                            .setIssuedAt(date)
                            .signWith(key, signatureAlgorithm)
                            .compact();
        } catch (Exception e) {
            log.error("리프레시 토큰으로 부터 액세스 토큰을 생성 실패 했습니다.: {}", e.getMessage());
            throw new RuntimeException("리프레시 토큰으로 부터 액세스 토큰을 생성 실패 했습니다.");
        }
    }

    public void handleExpiredAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String accessToken = getTokenFromHeader(request);
        if (accessToken != null && accessToken.startsWith(BEARER_PREFIX)) {
            String refreshToken = getRefreshTokenFromRedis(accessToken); // 레디스에서 리프레시 토큰 조회
            if (refreshToken != null) {
                // 유효한 리프레시 토큰인 경우, 새로운 액세스 토큰 발급
                String newAccessToken = createAccessTokenFromRefreshToken(refreshToken);
                addJwtHeader(newAccessToken, response);
            } else {
                log.error("유효하지 않은 리프레시 토큰입니다.");
            }
        } else {
            log.error("리프레시 토큰이 존재하지 않습니다.");
        }
    }
    private String getRefreshTokenFromRedis(String accessToken) {
        String refreshToken = null;
        try {
            Claims claims = getUserInfoFromToken(accessToken.substring(BEARER_PREFIX.length()));
            String email = claims.getSubject();
            Optional<RefreshToken> refreshTokenOptional = refreshTokenRedisRepository.findById(email);
            if (refreshTokenOptional.isPresent()) {
                refreshToken = refreshTokenOptional.get().getRefreshToken();
            }
        } catch (Exception e) {
            log.error("레디스에서 리프레시 토큰 조회 중 예외 발생: {}", e.getMessage());
        }
        return refreshToken;
    }
}
