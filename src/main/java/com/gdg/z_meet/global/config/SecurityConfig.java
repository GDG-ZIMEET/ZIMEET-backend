package com.gdg.z_meet.global.config;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


//@Configuration
//@RequiredArgsConstructor
//@EnableWebSecurity
//public class SecurityConfig {
//    private final JwtUtil jwtUtil;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
//                .cors(withDefaults()) // WebMvcConfigurer 에서 설정한 CORS 적용
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안 함
//                )
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/jwt/**", "/swagger-ui/**", "/v3/api-docs/**", "/booths/**").permitAll() // 인증 없이 접근 허용
//                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
//                )
//                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @RequiredArgsConstructor
//    @Configuration
//    public static class WebConfig implements WebMvcConfigurer {
//
//        @Bean
//        public WebMvcConfigurer webMvcConfigurer() {
//            return new WebMvcConfigurer() {
//
//                @Override
//                public void addCorsMappings(CorsRegistry registry) {
//                    registry.addMapping("/**")
//                            .allowedOrigins("http://localhost:3000") // 모든 Origin 허용
//                            .allowedMethods(
//                                    HttpMethod.GET.name(),
//                                    HttpMethod.HEAD.name(),
//                                    HttpMethod.POST.name(),
//                                    HttpMethod.PUT.name(),
//                                    HttpMethod.DELETE.name(),
//                                    HttpMethod.OPTIONS.name(),
//                                    HttpMethod.PATCH.name())
//                            .allowedHeaders(
//                                    "Authorization",
//                                    "Content-Type",
//                                    "Accept",
//                                    "X-Requested-With",
//                                    "Origin",
//                                    "Access-Control-Request-Method",
//                                    "Access-Control-Request-Headers")
//                            .allowCredentials(true);
//                }
//            };
//        }
//    }
//}
//

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 명확히 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안 함
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/jwt/**", "/swagger-ui/**", "/v3/api-docs/**", "/booths/**","/ws/**").permitAll() // 인증 없이 접근 허용
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안 함
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll() // 모든 요청 허용 (테스트용)
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // 허용할 Origin
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); // 허용할 HTTP 메서드
        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept", "X-Requested-With",
                "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));
        configuration.setAllowCredentials(true); // 쿠키 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
