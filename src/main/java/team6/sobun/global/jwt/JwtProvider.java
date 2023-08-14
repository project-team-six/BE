package team6.sobun.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
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
import team6.sobun.global.utils.EncryptionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_KEY = "auth";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 20 * 60 * 1000L; // 20분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L; // 1주일

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final EncryptionUtils encryptionUtils;


    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private ObjectMapper customObjectMapper = new ObjectMapper();

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
     * 헤더에 JWT 토큰을 추가합니다.
     *
     * @param accessToken  액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param response     HttpServletResponse 객체
     */
    public void addJwtHeaders(String accessToken, String refreshToken, HttpServletResponse response) {
        try {
             accessToken = URLEncoder.encode(accessToken, "utf-8").replaceAll("\\+", "%20");
             refreshToken = URLEncoder.encode(refreshToken, "utf-8").replaceAll("\\+", "%20");

            response.setHeader(AUTHORIZATION_HEADER, accessToken);
            response.setHeader("Refresh-Token", refreshToken);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
    }
    public String getNickNameFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.get("nickname", String.class);
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

    public String getRefreshTokenFromHeader(HttpServletRequest req) {
        String refreshToken = req.getHeader("Refresh-Token");
        if (refreshToken != null) {
            try {
                return URLDecoder.decode(refreshToken, "UTF-8");
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
    public String createToken(String userId, String username, String nickname, UserRoleEnum role, String profileImageUrl) {
        Date date = new Date();
        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username)
                        .claim("userId", userId)
                        .claim("nickname", nickname)
                        .claim(AUTHORIZATION_KEY, role)
                        .claim("profileImageUrl", profileImageUrl)
                        .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_EXPIRE_TIME)) // 만료시간
                        .setIssuedAt(date)
                        .signWith(key, signatureAlgorithm)
                        .compact();
    }

    public String createRefreshToken(String userId, String username, String nickname, UserRoleEnum role, String profileImageUrl) {
        Date date = new Date();
        String refreshToken = Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("nickname", nickname)
                .claim(AUTHORIZATION_KEY, role)
                .claim("profileImageUrl", profileImageUrl)
                .setExpiration(new Date(date.getTime() + REFRESH_TOKEN_EXPIRE_TIME)) // 만료시간
                .setIssuedAt(date)
                .signWith(key, signatureAlgorithm)
                .compact();

        try {
            return encryptionUtils.encrypt(refreshToken); // encryptionUtils 인스턴스를 통해 encrypt 메서드 호출
        } catch (Exception e) {
            log.error("리프레시 토큰 암호화 실패: {}", e.getMessage());
            throw new RuntimeException("리프레시 토큰 암호화 실패");
        }
    }
    // JwtProvider 클래스에 추가 메소드
    public String decryptRefreshToken(String encryptedRefreshToken) {
        try {
            return encryptionUtils.decrypt(encryptedRefreshToken);
        } catch (Exception e) {
            log.error("리프레시 토큰 복호화 실패: {}", e.getMessage());
            throw new RuntimeException("리프레시 토큰 복호화 실패");
        }
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
            refreshToken = refreshToken.replace("Bearer ", ""); // Bearer 접두사 제거

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .deserializeJsonWith(new JacksonDeserializer<>(customObjectMapper))
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            String id = claims.getSubject();
            UserRoleEnum role = UserRoleEnum.valueOf(claims.get(AUTHORIZATION_KEY, String.class));
            Date date = new Date();

            String newAccessToken = BEARER_PREFIX +
                    Jwts.builder()
                            .setSubject(id) // 사용자 식별
                            .claim("userId", claims.get("userId", String.class)) // userId 추가
                            .claim("nickname", claims.get("nickname", String.class))
                            .claim(AUTHORIZATION_KEY, role)
                            .claim("profileImageUrl", claims.get("profileImageUrl", String.class))
                            .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_EXPIRE_TIME)) // 만료 시간
                            .setIssuedAt(date)
                            .signWith(key, signatureAlgorithm)
                            .compact();

            log.info("리프레시 토큰으로부터 새로운 액세스 토큰 생성");
            return newAccessToken;
        } catch (Exception e) {
            log.error("리프레시 토큰으로부터 액세스 토큰 생성 실패: {}", e.getMessage());
            throw new RuntimeException("리프레시 토큰으로부터 액세스 토큰 생성 실패");
        }
    }
    /**
     * 주어진 리프레시 토큰을 사용하여 새로운 액세스 토큰을 생성하고 인증 설정을 갱신합니다.
     *
     * @param refreshTokenValue 복호화된 리프레시 토큰
     * @param response          HttpServletResponse 객체
     */
    public void refreshAccessToken(String refreshTokenValue, HttpServletResponse response) {
        if (StringUtils.hasText(refreshTokenValue)) {
            String decryptedRefreshToken = decryptRefreshToken(refreshTokenValue);
            log.info("복호화 넘어왔나?={}", decryptedRefreshToken);
            if (StringUtils.hasText(decryptedRefreshToken) && validateToken(decryptedRefreshToken)) {
                String redisStoredRefreshToken = getRefreshTokenFromRedis(decryptedRefreshToken); // 이 부분 수정
                log.info("레디스에서 리프레시 토큰 넘어왔나?={}", redisStoredRefreshToken);
                if (redisStoredRefreshToken != null && redisStoredRefreshToken.equals(decryptedRefreshToken)) {
                    String newAccessToken = createAccessTokenFromRefreshToken(decryptedRefreshToken);
                    addJwtHeader(newAccessToken, response);
                    log.info("리프레시 토큰으로 새로운 액세스 토큰 발급");
                } else {
                    log.error("레디스에 저장된 리프레시 토큰과 일치하지 않습니다.");
                }
            } else {
                log.error("리프레시 토큰이 유효하지 않습니다.");
            }
        } else {
            log.error("리프레시 토큰이 없습니다.");
        }
    }


    public String getRefreshTokenFromRedis(String decryptedRefreshToken) {
        String refreshToken = null;
        try {
            Claims claims = getUserInfoFromToken(decryptedRefreshToken);

            if (claims != null) {
                String refreshTokenKey = claims.getSubject(); // 리프레시 토큰 키 생성
                log.info("레디스에서 키 값으로 값 조회를 시도 중: " + refreshTokenKey);
                refreshToken = refreshTokenRedisRepository.findById(refreshTokenKey)
                        .map(RefreshToken::getRefreshToken)
                        .orElse(null);
                if (refreshToken != null) {
                    log.info("레디스에서 리프레시 토큰 조회 성공");
                    // 리프레시 토큰 복호화를 위한 부분 추가
                    refreshToken = decryptRefreshToken(refreshToken);
                } else {
                    log.error("레디스에서 리프레시 토큰을 찾을 수 없습니다.");
                }
            }
        } catch (Exception e) {
            log.error("레디스에서 리프레시 토큰 조회 실패 : {}", e.getMessage());
        }
        log.info("리프레시 토큰 조회 성공");
        return refreshToken;
    }


    public void expireAccessToken(String token, HttpServletResponse response) {
        try {
            // 디코딩된 토큰 추출
            String decodedToken = URLDecoder.decode(token, "UTF-8");
            // "Bearer " 제거
            String cleanToken = decodedToken.replace("Bearer ", "");
            Claims claims = getUserInfoFromToken(cleanToken);
            if (claims != null) {
                // 기존 토큰의 만료 시간을 현재 시간으로 설정하여 즉시 만료
                Date expiration = new Date();

                // 기존 토큰을 업데이트하여 만료시킴
                String expireAccessToken =
                        BEARER_PREFIX +
                        Jwts.builder()
                        .setClaims(claims)
                        .setExpiration(expiration)
                        .signWith(key, signatureAlgorithm)
                        .compact();

                // 새로 생성된 액세스 토큰으로 헤더 업데이트
                addJwtHeader(expireAccessToken, response);

                log.info("액세스 토큰을 강제로 만료시킴");
            }
        } catch (Exception e) {
            log.error("액세스 토큰 만료 실패: {}", e.getMessage());
            throw new RuntimeException("액세스 토큰 만료 실패");
        }
    }
}
