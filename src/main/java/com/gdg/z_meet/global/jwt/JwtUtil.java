package com.gdg.z_meet.global.jwt;

import com.gdg.z_meet.domain.user.dto.Token;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;


@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final UserDetailsService userDetailsService;
    // 유효기간 7일
    private long accessTokenValidTime = 31 * 24 * 60 * 60 * 1000L;
    // 유효기간 31일
    private long refreshTokenValidTime = 31 * 24 * 60 * 60 * 1000L;

    @Value("${jwt.secret}")
    private String secretKey ;
    private Key key;

    // 객체 초기화, secretKey를 Base64로 인코딩
    @PostConstruct
    protected void init(){
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public Token createToken(String studentNumber) {
        Date now = new Date();

        String accessToken = getToken(studentNumber, now, accessTokenValidTime);
        String refreshToken = getToken(studentNumber, now, refreshTokenValidTime);

        return Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .key(studentNumber)
                .build();
    }

    public String getToken(String studentNumber, Date currentTime, long validTime) {
        return Jwts.builder()
                .setSubject(studentNumber)
                .setIssuedAt(currentTime)
                .setExpiration(new Date(currentTime.getTime() + validTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 인증 정보 조회
    public Authentication getAuthentication(String token){
        validationAuthorizationHeader(token);
        String available_token = extractToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getStudentNumberFromToken(available_token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰에서 회원 정보 추출
    public String getStudentNumberFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }


    // Request Header에서 token 값 가져오기
    public String getAccessToken(HttpServletRequest request) { return request.getHeader("Authorization");}

    private String extractToken(String authorizationHeader){
        return authorizationHeader.substring("Bearer ".length()).trim();
    }

    private void validationAuthorizationHeader(String header){
        if(header == null || !header.startsWith("Bearer ")){
            throw new IllegalArgumentException();
        }
    }

    // 토큰 유효성 검사
    public boolean validateToken(ServletRequest request, String jwtToken){
        try {
            validationAuthorizationHeader(jwtToken);
            String token = extractToken(jwtToken);
            userDetailsService.loadUserByUsername(this.getStudentNumberFromToken(token));
            Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (SignatureException e) {
            e.printStackTrace();
            request.setAttribute("exception", "ForbidddenException");
        } catch (MalformedJwtException e) {
            e.printStackTrace();
            request.setAttribute("exception", "MalformedJwtException");
        } catch (ExpiredJwtException e) {
            //토큰 만료시
            e.printStackTrace();
            request.setAttribute("exception", "ExpiredJwtException");
        } catch (UnsupportedJwtException e) {
            e.printStackTrace();
            request.setAttribute("exception", "UnsupportedJwtException");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            request.setAttribute("exception", "IllegalArgumentException");
        }
        return false;
    }
}
