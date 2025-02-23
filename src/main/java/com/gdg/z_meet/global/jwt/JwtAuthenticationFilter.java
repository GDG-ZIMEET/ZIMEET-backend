package com.gdg.z_meet.global.jwt;

import com.gdg.z_meet.domain.user.entity.RefreshToken;
import com.gdg.z_meet.domain.user.repository.RefreshTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String accessToken = jwtUtil.getAccessToken(request);

        if (accessToken != null && jwtUtil.validateToken(request, accessToken)) {
            Authentication authentication = jwtUtil.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            // 액세스 토큰이 없거나 만료되었을 경우, 리프레시 토큰 확인
            String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);

            if (refreshToken != null && jwtUtil.validateToken(request, refreshToken)) {
                Optional<RefreshToken> storedToken = refreshTokenRepository.findByRefreshToken(refreshToken);

                if (storedToken.isPresent()) {
                    String studentNumber = jwtUtil.getStudentNumberFromToken(refreshToken);
                    Long userId = jwtUtil.getUserIdFromToken(refreshToken);

                    // 새로운 액세스 토큰 발급
                    String newAccessToken = jwtUtil.createToken(studentNumber, userId).getAccessToken();
                    response.setHeader("Authorization", "Bearer " + newAccessToken);

                    // 인증 정보 설정
                    Authentication authentication = jwtUtil.getAuthentication(newAccessToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
