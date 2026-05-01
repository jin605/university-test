package com.beyond.university.config;

import com.beyond.university.auth.handler.AccessDeniedHandlerImpl;
import com.beyond.university.auth.handler.AuthenticationEntryPointImpl;
import com.beyond.university.auth.jwt.JwtAuthenticationFilter;
import com.beyond.university.auth.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   JwtTokenProvider jwtTokenProvider) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(corsConfigurer ->
                        corsConfigurer.configurationSource(getCorsConfigurationSource())
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // JWT 토큰으로 인증을 처리하기 때문에 세션을 사용하지 않는다.
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 추가하는 설정
                // JwtAuthenticationFilter에서 인증이 정상적으로 처리되면 UsernamePasswordAuthenticationFilter는 자동으로 통과한다.
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                // 인증되지 않은 사용자가 리소스에 접근할 때 호출되는 핸들러이다.
                                // 401 Unauthorized 응답을 하도록 구현한다.
                                .authenticationEntryPoint(new AuthenticationEntryPointImpl())
                                // 인증된 사용자가 권한이 없는 리소스에 접근할 때 호출되는 핸들러이다.
                                // 403 Forbidden 응답을 하도록 구현한다.
                                .accessDeniedHandler(new AccessDeniedHandlerImpl())
                )
                .authorizeHttpRequests(authorizationRequests ->
                        authorizationRequests
                                // Swagger & Actuator
                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/actuator/health/**").permitAll()
                                // 로그인, 토큰 재발급
                                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh"). permitAll()
                                // 모든 GET 요청의 경우 허용
                                // .requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()
                                // 모든 DELETE 요청의 경우 관리자만 허용
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")
                                // 이 외의 모든 요청은 인증이 필요
                                .anyRequest().authenticated()
                );

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    private static CorsConfigurationSource getCorsConfigurationSource() {

        return (request) -> {
            // CorsConfiguration 객체를 생성해서 CORS 설정을 한다.
            CorsConfiguration corsConfiguration = new CorsConfiguration();

            // CORS 요청에서 허용할 출처를 지정한다.
            // corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:5174"));
            corsConfiguration.setAllowedOriginPatterns(List.of("*"));

            // CORS 요청에서 허용할 HTTP 메소드를 지정한다.
            corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

            // 클라이언트가 요청 시 사용할 수 있는 헤더를 지정한다.
            corsConfiguration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

            // 클라이언트가 응답에서 접근할 수 있는 헤더를 지정한다.
            corsConfiguration.setExposedHeaders(List.of("Authorization"));

            // 자격 증명(쿠키, 세션) 허용 여부를 설정한다.
            corsConfiguration.setAllowCredentials(true);

            // CORS Preflight 요청을 브라우저가 캐싱하는 시간(초 단위)을 설정한다.
            corsConfiguration.setMaxAge(3600L);

            return corsConfiguration;
        };
    }
}
