package com.gdg.z_meet.global.config;

import com.gdg.z_meet.global.jwt.JwtAuthenticationFilter;
import com.gdg.z_meet.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
//        configuration.setAllowedOrigins(List.of("localhost:3000", "localhost:8080", "http://52.79.78.62:8081"));
//        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
//        configuration.setAllowCredentials(true);  // 쿠키 및 인증 정보 허용
//
//        configuration.addExposedHeader("Access-Control-Allow-Origin");
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//
//    @Bean
//    public FilterRegistrationBean<CorsFilter> corsFilter() {
//        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource()));
//        bean.setOrder(Ordered.HIGHEST_PRECEDENCE); // 필터 우선순위를 가장 높게 설정
//        return bean;
//    }
//
//}
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .cors(withDefaults()) // WebMvcConfigurer 에서 설정한 CORS 적용
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안 함
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/jwt/**", "/swagger-ui/**", "/v3/api-docs/**", "/booths/**").permitAll() // 인증 없이 접근 허용
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @RequiredArgsConstructor
    @Configuration
    public static class WebConfig implements WebMvcConfigurer {

        @Bean
        public WebMvcConfigurer webMvcConfigurer() {
            return new WebMvcConfigurer() {

                @Override
                public void addCorsMappings(CorsRegistry registry) {
                    registry.addMapping("/**")
                            .allowedOrigins("*") // 모든 Origin 허용
                            .allowedMethods(
                                    HttpMethod.GET.name(),
                                    HttpMethod.HEAD.name(),
                                    HttpMethod.POST.name(),
                                    HttpMethod.PUT.name(),
                                    HttpMethod.DELETE.name(),
                                    HttpMethod.OPTIONS.name(),
                                    HttpMethod.PATCH.name())
                            .allowedHeaders("*");
//                            .allowCredentials(true);
                }
            };
        }
    }
}

