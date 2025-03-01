package com.gdg.z_meet.global.config;

import com.gdg.z_meet.domain.user.repository.RefreshTokenRepository;
import com.gdg.z_meet.global.jwt.JwtAuthenticationFilter;
import com.gdg.z_meet.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RefreshTokenRepository refreshTokenRepository) throws Exception {
        http
                .cors(cors -> cors.configure(http))  // CORS 설정 활성화(nginx 에서 처리)
                .csrf(csrf -> csrf.disable())        // CSRF 비활성화
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안 함
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/static/**", "/favicon.ico").permitAll() // 정적 파일 허용
                        .requestMatchers("/swagger", "/swagger/", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger 허용
                        .requestMatchers("/api/user/**", "/api/booths/**").permitAll()   // /api 이하 경로 접근 허용
                        .requestMatchers("/","/api/health").permitAll() // 인증 없이 접근 허용
                        .requestMatchers("/ws", "/ws/**", "/ws/info/**").permitAll()  // WebSocket 엔드포인트 허용
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, refreshTokenRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}