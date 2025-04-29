package Challenge.with_back.security;

import Challenge.with_back.security.filter.JwtFilter;
import Challenge.with_back.security.exception.CustomAccessDeniedHandler;
import Challenge.with_back.security.exception.CustomAuthenticationEntryPoint;
import Challenge.with_back.security.handler.LoginFailureHandler;
import Challenge.with_back.security.handler.LoginSuccessHandler;
import Challenge.with_back.security.handler.OAuth2SuccessHandler;
import Challenge.with_back.security.oauth2.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig
{
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;

    // 필터 클래스
    private final JwtFilter jwtFilter;

    // 예외 처리 클래스
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    // 인증 성공 및 실패 처리 핸들러
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    // 비밀번호 인코더 설정
    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception
    {
        // 기본 설정
        httpSecurity
                .httpBasic(HttpBasicConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(c -> c.frameOptions(
                        HeadersConfigurer.FrameOptionsConfig::disable).disable())
                .sessionManagement(c -> c.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS));

        // 필터 설정
        httpSecurity
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // 일반 로그인 설정
        httpSecurity
                .formLogin(loginConfig -> loginConfig
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .loginProcessingUrl("/api/login")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler))
                .userDetailsService(customUserDetailsService);

        // OAuth 2.0 로그인 설정
        httpSecurity
                .oauth2Login(loginConfig -> loginConfig
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                );

        // 예외 처리 설정
        httpSecurity
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));

        return httpSecurity.build();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
