package com.gdg.z_meet.domain.chat.controller;


import com.gdg.z_meet.domain.chat.dto.ChatMessage;
import com.gdg.z_meet.domain.chat.service.ChatRoomService;
import com.gdg.z_meet.domain.chat.service.ChatService;
import com.gdg.z_meet.domain.chat.service.MessageService;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import org.springframework.messaging.handler.annotation.Header;
import com.gdg.z_meet.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatService chatService; // ChatService 추가

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, @Payload ChatMessage chatMessage, @Header("Authorization") String token) {
        Long senderId = jwtUtil.extractUserIdFromToken(token);
        User user = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(Code.MEMBER_NOT_FOUND));

        chatMessage = ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .type(chatMessage.getType())
                .roomId(roomId)
                .senderName(user.getUserProfile().getNickname())
                .content(chatMessage.getContent())
                .sendAt(LocalDateTime.now())
                .emoji(user.getUserProfile().getEmoji())
                .build();

        switch (chatMessage.getType()) {
            case ENTER:
                chatService.handleEnterMessage(roomId, chatMessage.getContent());
                break;
            case TALK:
                chatService.handleTalkMessage(chatMessage);
                break;
            case EXIT:
                chatService.handleExitMessage(roomId, chatMessage.getSenderName());
                break;
            default:
                chatService.handleTalkMessage(chatMessage);
        }
    }
}
