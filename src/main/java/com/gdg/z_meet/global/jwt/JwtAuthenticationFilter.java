package com.gdg.z_meet.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String accessToken = jwtUtil.getAccessToken(request);

        // 액세스 토큰이 유효한 경우
        if (accessToken != null && jwtUtil.validateToken(request, accessToken)) {
            Authentication authentication = jwtUtil.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 액세스 토큰이 없거나 만료된 경우, 리프레시 토큰 확인
        else {
            String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);

            if (refreshToken != null && jwtUtil.validateRefreshToken(refreshToken)) {
                // 리프레시 토큰에서 정보 추출
                String studentNumber = jwtUtil.getStudentNumberFromToken(refreshToken);
                Long userId = jwtUtil.getUserIdFromToken(refreshToken);

                // 새로운 액세스 토큰 발급
                String newAccessToken = jwtUtil.getToken(studentNumber, userId, new java.util.Date(), jwtUtil.getAccessTokenValidTime());
                response.setHeader("Authorization", "Bearer " + newAccessToken);

                // 인증 정보 설정
                Authentication authentication = jwtUtil.getAuthentication("Bearer " + newAccessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}