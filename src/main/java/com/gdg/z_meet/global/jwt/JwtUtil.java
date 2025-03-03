package com.gdg.z_meet.global.jwt;

import com.gdg.z_meet.domain.user.dto.Token;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final long ACCESS_TOKEN_VALID_TIME = 7 * 24 * 60 * 60 * 1000L;  // 7일
    private static final long REFRESH_TOKEN_VALID_TIME = 31 * 24 * 60 * 60 * 1000L; // 31일

    private final UserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;
    private JwtParser jwtParser;

    @PostConstruct
    protected void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
        jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    public Token createToken(String studentNumber, Long id) {
        Date now = new Date();
        String accessToken = getToken(studentNumber, id, now, ACCESS_TOKEN_VALID_TIME);
        String refreshToken = getToken(studentNumber, id, now, REFRESH_TOKEN_VALID_TIME);
        return Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .key(studentNumber)
                .userId(id)
                .build();
    }

    public String getToken(String studentNumber, Long id, Date currentTime, long validTime) {
        return Jwts.builder()
                .setSubject(studentNumber)
                .claim("user_id", id)
                .setIssuedAt(currentTime)
                .setExpiration(new Date(currentTime.getTime() + validTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Cookie createCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (REFRESH_TOKEN_VALID_TIME / 1000));
        response.addCookie(cookie);
        log.info("Refresh token set in cookie: name={}, value={}, maxAge={}",
                REFRESH_TOKEN_COOKIE, refreshToken, cookie.getMaxAge());
        return cookie;
    }

    public String extractKeyIdFromAccessToken(String accessToken) {
        String availableToken = extractTokenFromHeader(accessToken);
        return jwtParser.parseClaimsJws(availableToken).getBody().getSubject();
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            log.warn("No cookies found in request");
            return null;
        }
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        log.info("Refresh token from cookie: {}", refreshToken != null ? refreshToken : "Not found");
        return refreshToken;
    }

    public Authentication getAuthentication(String token) {
        String availableToken = extractTokenFromHeader(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(getStudentNumberFromToken(availableToken));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getStudentNumberFromToken(String token) {
        return jwtParser.parseClaimsJws(token).getBody().getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return jwtParser.parseClaimsJws(token).getBody().get("user_id", Long.class);
    }

    public String getAccessToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new JwtValidationException("유효하지 않은 토큰 형식입니다.");
        }
        return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            jwtParser.parseClaimsJws(refreshToken);
            return true;
        } catch (JwtException e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateToken(ServletRequest request, String jwtToken) {
        try {
            String token = extractTokenFromHeader(jwtToken);
            userDetailsService.loadUserByUsername(getStudentNumberFromToken(token));
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            request.setAttribute("exception", e.getClass().getSimpleName());
            return false;
        }
    }

    public Long extractUserIdFromToken(String token) {
        String availableToken = extractTokenFromHeader(token);
        return getUserIdFromToken(availableToken);
    }

    public Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = getAccessToken(request);
        return extractUserIdFromToken(token); // extractTokenFromHeader에서 예외 처리
    }

    public long getAccessTokenValidTime() {
        return ACCESS_TOKEN_VALID_TIME;
    }

    public long getRefreshTokenValidTime() {
        return REFRESH_TOKEN_VALID_TIME;
    }
}