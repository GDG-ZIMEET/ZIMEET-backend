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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 헤더에서 액세스 토큰을 받아옴
        String accessToken = jwtUtil.getAccessToken(request);

        if (accessToken != null && jwtUtil.validateToken(request, accessToken)) {
            Authentication authentication = jwtUtil.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            // 쿠키에서 리프레쉬 토큰을 가져옴
            String refreshToken = jwtUtil.getValidRefreshToken(request.getCookies());

            if (refreshToken != null) {
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
