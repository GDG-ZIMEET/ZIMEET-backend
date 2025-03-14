package com.gdg.z_meet.global.jwt;

import com.gdg.z_meet.domain.user.repository.RefreshTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String accessToken = jwtUtil.getAccessToken(request);
        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);

        log.debug("Request URI: {}, Access Token: {}, Refresh Token: {}", uri, accessToken, refreshToken);

        if (uri.equals("/api/user/signup") || uri.equals("/api/user/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (accessToken != null && jwtUtil.validateToken(request, accessToken)) {
                setAuthentication(accessToken);
                log.info("Access token validated successfully");
            } else if (refreshToken != null) {
                log.info("Attempting to validate refresh token: {}", refreshToken);
                if (jwtUtil.validateRefreshToken(refreshToken)) {
                    String userIdStr = refreshTokenRepository.findByToken(refreshToken);
                    log.info("User ID found in Redis: {}", userIdStr);
                    if (userIdStr != null) {
                        Long userId = Long.parseLong(userIdStr);
                        String studentNumber = jwtUtil.getStudentNumberFromToken(refreshToken);
                        String newAccessToken = jwtUtil.getToken(studentNumber, userId, new Date(), jwtUtil.getAccessTokenValidTime());
                        response.setHeader("Authorization", BEARER_PREFIX + newAccessToken);
                        // 요청 객체에 새 토큰 반영
                        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                            @Override
                            public String getHeader(String name) {
                                if ("Authorization".equalsIgnoreCase(name)) {
                                    return BEARER_PREFIX + newAccessToken;
                                }
                                return super.getHeader(name);
                            }
                        };
                        setAuthentication(newAccessToken);
                        log.info("Access token refreshed for userId: {}", userId);
                        filterChain.doFilter(wrappedRequest, response); // 수정된 요청 전달
                        return;
                    } else {
                        log.warn("Refresh token not found in Redis: {}", refreshToken);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                } else {
                    log.warn("Refresh token validation failed: {}", refreshToken);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } else {
                log.warn("No valid access or refresh token provided");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT processing failed: {}", e.getMessage());
            sendErrorResponse(response, "Authentication failed", HttpStatus.UNAUTHORIZED);
            return;
        }
    }

    private void setAuthentication(String token) {
        Authentication authentication = jwtUtil.getAuthentication(token);
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authentication set successfully for token: {}", token);
        } else {
            log.warn("Authentication object is null for token: {}", token);
            throw new JwtValidationException("Failed to create authentication from token");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }
}