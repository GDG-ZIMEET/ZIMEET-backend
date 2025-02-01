package com.gdg.z_meet.domain.chat.service;

import com.gdg.z_meet.domain.chat.dto.ChatMessage;
import com.gdg.z_meet.domain.chat.dto.MessageDto;
import com.gdg.z_meet.domain.chat.entity.status.MessageType;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public void handleEnterMessage(Long roomId, String content) {
        Long inviteId;
        try {
            inviteId = Long.parseLong(content);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid inviteId format: " + content);
        }

        User user = userRepository.findById(inviteId).orElseThrow(() -> new BusinessException(Code.MEMBER_NOT_FOUND));
        String inviteName = user.getUserProfile().getNickname();
        if (inviteName == null || inviteName.isEmpty()) {
            throw new IllegalArgumentException("Invitee name not found for ID: " + inviteId);
        }

        ChatMessage enterMessage = createChatMessage(roomId, inviteId, inviteName, MessageType.ENTER, inviteName + "님이 입장하셨습니다.");
        messageService.processMessage(enterMessage);
    }

    public void handleTalkMessage(Long roomId, Long senderId, MessageDto messageDto) {
        ChatMessage talkMessage = createChatMessage(roomId, senderId, messageDto.getSenderName(), MessageType.TALK, messageDto.getContent());
        messageService.processMessage(talkMessage);
    }

    public void handleExitMessage(Long roomId, Long senderId, String senderName) {
        ChatMessage exitMessage = createChatMessage(roomId, senderId, senderName, MessageType.EXIT, senderName + "님이 퇴장하셨습니다.");
        messageService.processMessage(exitMessage);
    }

    private ChatMessage createChatMessage(Long roomId, Long senderId, String senderName, MessageType type, String content) {
        // 고유한 ID 생성
        String uniqueId = UUID.randomUUID().toString();

        return new ChatMessage(
                uniqueId,
                type,
                roomId.toString(),
                senderId,
                senderName,
                content,
                LocalDateTime.now()
        );
    }
}
