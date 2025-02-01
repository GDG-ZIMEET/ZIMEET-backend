package com.gdg.z_meet.domain.chat.controller;


import com.gdg.z_meet.domain.chat.dto.MessageDto;
import com.gdg.z_meet.domain.chat.entity.status.MessageType;
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

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatService chatService; // ChatService 추가

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, @Payload MessageDto messageDto, @Header("Authorization") String token) {
        Long senderId = jwtUtil.extractUserIdFromToken(token);
        messageDto.setSenderId(senderId);
        User user = userRepository.findById(senderId).orElseThrow(() -> new BusinessException(Code.MEMBER_NOT_FOUND));
        String senderName = user.getUserProfile().getNickname();
        messageDto.setSenderName(senderName);

        switch (messageDto.getMessageType()) {
            case ENTER:
                chatService.handleEnterMessage(roomId, messageDto.getContent());
                break;
            case TALK:
                chatService.handleTalkMessage(roomId, senderId, messageDto);
                break;
            case EXIT:
                chatService.handleExitMessage(roomId, senderId, senderName);
                break;
            default:
                messageDto.setMessageType(MessageType.TALK);
                chatService.handleTalkMessage(roomId, senderId, messageDto);
        }
    }
}
