package com.gdg.z_meet.domain.chat.controller;


import com.gdg.z_meet.domain.chat.dto.ChatMessage;
import com.gdg.z_meet.domain.chat.service.ChatService;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import org.springframework.messaging.handler.annotation.Header;
import com.gdg.z_meet.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ChatService chatService;

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, @Payload ChatMessage chatMessage, @Header("Authorization") String token) {
        Long senderId = jwtUtil.extractUserIdFromToken(token);
        User user = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(Code.MEMBER_NOT_FOUND));
        chatMessage = ChatMessage.builder()
                .type(chatMessage.getType())
                .roomId(roomId)
                .senderId(senderId)
                .senderName(user.getUserProfile().getNickname())
                .content(chatMessage.getContent())
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
                chatService.handleExitMessage(roomId, senderId, chatMessage.getSenderName());
                break;
            default:
                chatService.handleTalkMessage(chatMessage);
        }
    }
}
