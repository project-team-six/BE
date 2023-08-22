package team6.sobun.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import team6.sobun.global.jwt.JwtAuthenticationFilter;
import team6.sobun.global.jwt.JwtAuthorizationFilter;
import team6.sobun.global.jwt.JwtExceptionFilter;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.security.UserDetailsServiceImpl;
import team6.sobun.global.security.repository.RefreshTokenRedisRepository;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final RefreshTokenRedisRepository redisRepository;

    /**
     * JwtExceptionFilter를 빈으로 생성합니다.
     * JWT 예외 처리를 담당합니다.
     *
     * @return JwtExceptionFilter 인스턴스
     */
    @Bean
    public JwtExceptionFilter jwtExceptionFilter() {
        return new JwtExceptionFilter(jwtProvider);
    }

    /**
     * AuthenticationManager를 빈으로 생성합니다.
     * 인증 매니저를 구성합니다.
     *
     * @param configuration AuthenticationConfiguration 인스턴스
     * @return AuthenticationManager 인스턴스
     * @throws Exception 예외 발생 시
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * JwtAuthenticationFilter를 빈으로 생성합니다.
     * JWT 인증 필터를 구성합니다.
     *
     * @return JwtAuthenticationFilter 인스턴스
     * @throws Exception 예외 발생 시
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtProvider, redisRepository);
        filter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
        return filter;
    }
    /**
     * JwtAuthorizationFilter를 빈으로 생성합니다.
     * JWT 인가 필터를 구성합니다.
     *
     * @return JwtAuthorizationFilter 인스턴스
     */
    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(jwtProvider, userDetailsService, redisRepository);
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("*");
        source.registerCorsConfiguration("/**",config);
        source.registerCorsConfiguration("/chat", config);
        return new CorsFilter(source);
    }

    /**
     * SecurityFilterChain을 설정합니다.
     * CSRF 설정, CORS 설정, 세션 생성 방식을 JWT 방식으로 변경합니다.
     * 요청 경로에 따른 인증 및 인가를 처리합니다.
     *
     * @param http HttpSecurity 인스턴스
     * @return SecurityFilterChain 인스턴스
     * @throws Exception 예외 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF 설정, CORS 설정, 기존 세션 방식 -> JWT 방식
        http
                .csrf((csrf) -> csrf.disable()) // CSRF 비활성화
                .cors(withDefaults()) // 기본 CORS 설정 적용
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 관리 방식 설정
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                                .requestMatchers(GET,"/**").permitAll()
                                .requestMatchers("/auth/**").permitAll()
                                .requestMatchers("/auth/email/**").permitAll()
                                .requestMatchers("/auth/admin/{userId}").permitAll()
                                .requestMatchers("/auth/naver/login").permitAll()
                                .requestMatchers("/auth/naver").permitAll()
                                .requestMatchers(GET,"/auth/mypage/pin").permitAll()
                                .requestMatchers("/auth/mypage/{id}").permitAll()
                                .requestMatchers("/auth/kakao").permitAll()
                                .requestMatchers("/auth/kakao/logout").permitAll()
                                .requestMatchers("/auth/kakao/login").permitAll()
                                .requestMatchers("/auth/google").permitAll()
                                .requestMatchers("/auth/google/login").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers(GET,"/post/**").permitAll()
                                .requestMatchers(GET,"/test").permitAll()
                                .requestMatchers("/chat/image").permitAll()
                                .requestMatchers("/chat/{roomId}").permitAll()
                                .anyRequest().authenticated()) // 그 외 모든 요청 인증처리
                .addFilter(corsFilter())
                .addFilterBefore(jwtAuthorizationFilter(), JwtAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter(), JwtAuthenticationFilter.class);
        return http.build();
    }

}

