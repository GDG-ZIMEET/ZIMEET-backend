package com.gdg.z_meet.global.jwt;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    public StompAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token == null || !jwtUtil.validateToken(null, token)) {
                throw new IllegalArgumentException("Invalid or Missing Token");
            }

            // 토큰에서 사용자 정보 추출
            String studentNumber = jwtUtil.getStudentNumberFromToken(token);

            // 인증된 사용자 설정 (Principal 구현)
            accessor.setUser(() -> studentNumber);
        }

        return message;
    }
}
