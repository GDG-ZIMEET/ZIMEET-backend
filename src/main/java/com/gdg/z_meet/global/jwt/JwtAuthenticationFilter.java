package com.gdg.z_meet.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String accessToken = jwtUtil.getAccessToken(request);
        log.debug("Access token from request: {}", accessToken);

        try {
            // 액세스 토큰이 유효한 경우
            if (accessToken != null && jwtUtil.validateToken(request, accessToken)) {
                setAuthentication(accessToken);
                log.info("Access token validated successfully");
            }
            // 액세스 토큰이 없거나 만료된 경우, 리프레시 토큰 확인
            else {
                String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);
                log.debug("Refresh token from cookie: {}", refreshToken);

                if (refreshToken != null && jwtUtil.validateRefreshToken(refreshToken)) {
                    // 리프레시 토큰에서 정보 추출
                    String studentNumber = jwtUtil.getStudentNumberFromToken(refreshToken);
                    Long userId = jwtUtil.getUserIdFromToken(refreshToken);

                    // 새로운 액세스 토큰 발급
                    String newAccessToken = jwtUtil.getToken(studentNumber, userId, new java.util.Date(), jwtUtil.getAccessTokenValidTime());
                    response.setHeader("Authorization", BEARER_PREFIX + newAccessToken);
                    log.info("New access token issued: studentNumber={}, token={}", studentNumber, newAccessToken);

                    // 인증 정보 설정
                    setAuthentication(BEARER_PREFIX + newAccessToken);
                } else {
                    log.warn("Invalid or missing refresh token");
                }
            }
        } catch (JwtValidationException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            sendErrorResponse(response, e.getMessage(), HttpStatus.UNAUTHORIZED);
            return; // 필터 체인 중단
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token) {
        Authentication authentication = jwtUtil.getAuthentication(token);
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("Failed to create authentication from token");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }
}