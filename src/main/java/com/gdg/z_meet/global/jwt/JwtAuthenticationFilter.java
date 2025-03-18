package com.gdg.z_meet.global.jwt;

import com.gdg.z_meet.domain.user.entity.User;
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
import org.springframework.security.core.userdetails.UserDetails;
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String accessToken = jwtUtil.getAccessToken(request);
        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);

        log.debug("Request URI: {}, Access Token: {}, Refresh Token: {}", uri, accessToken, refreshToken);

        // 회원가입/로그인 API는 필터 제외
        if (uri.equals("/api/user/signup") || uri.equals("/api/user/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (accessToken != null && jwtUtil.validateToken(request, accessToken)) {
                setAuthentication(accessToken, response);
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

                        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                            @Override
                            public String getHeader(String name) {
                                if ("Authorization".equalsIgnoreCase(name)) {
                                    return BEARER_PREFIX + newAccessToken;
                                }
                                return super.getHeader(name);
                            }
                        };
                        setAuthentication(newAccessToken, response);
                        log.info("Access token refreshed for userId: {}", userId);
                        filterChain.doFilter(wrappedRequest, response);
                        return;
                    } else {
                        log.warn("Refresh token not found in Redis: {}", refreshToken);
                        sendErrorResponse(response, "Invalid refresh token", HttpStatus.UNAUTHORIZED);
                        return;
                    }
                } else {
                    log.warn("Refresh token validation failed: {}", refreshToken);
                    sendErrorResponse(response, "Invalid refresh token", HttpStatus.UNAUTHORIZED);
                    return;
                }
            } else {
                log.warn("No valid access or refresh token provided");
                sendErrorResponse(response, "Authentication required", HttpStatus.UNAUTHORIZED);
                return;
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT processing failed: {}", e.getMessage());
            sendErrorResponse(response, "Authentication failed", HttpStatus.UNAUTHORIZED);
        }
    }

    private void setAuthentication(String token, HttpServletResponse response) throws IOException {
        Authentication authentication = jwtUtil.getAuthentication(token);
        if (authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = (User) userDetails;
            if (user.isDeleted()) {
                log.warn("Deleted user attempted access: {}", user.getUsername());
                sendErrorResponse(response, "User is deleted", HttpStatus.FORBIDDEN);
                return;
            }
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