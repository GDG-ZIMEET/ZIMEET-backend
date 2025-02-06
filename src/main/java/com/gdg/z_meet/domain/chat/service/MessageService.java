package com.gdg.z_meet.domain.chat.service;

import com.gdg.z_meet.domain.chat.dto.ChatMessage;
import com.gdg.z_meet.domain.chat.dto.ChatRoomDto;
import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import com.gdg.z_meet.domain.chat.entity.status.MessageType;
import com.gdg.z_meet.domain.chat.repository.ChatRoomRepository;
import com.gdg.z_meet.domain.chat.repository.JoinChatRepository;
import com.gdg.z_meet.domain.chat.repository.MessageRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.entity.UserProfile;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.domain.user.service.UserService;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MessageService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final JoinChatRepository joinChatRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String CHAT_ROOM_MESSAGES_KEY = "chatroom:%s:messages";
    private static final String CHAT_ROOM_LATEST_MESSAGE_KEY = "chatroom:%s:latestMessage";
    private static final String CHAT_ROOM_LATEST_MESSAGE_TIME_KEY = "chatroom:%s:latestMessageTime";

    @Transactional
    public void processMessage(ChatMessage chatMessage) {
        saveMessage(chatMessage);
        broadcastMessage(chatMessage);
    }

    @Transactional
    public void saveMessage(ChatMessage chatMessage) {
        Long chatRoomId = chatMessage.getRoomId();

        // Redis에 메시지 저장
        String chatRoomMessagesKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);
        redisTemplate.opsForList().rightPush(chatRoomMessagesKey, chatMessage);

        // 최신 메시지 및 활동 시간 업데이트
        String latestMessageKey = String.format(CHAT_ROOM_LATEST_MESSAGE_KEY, chatRoomId);
        redisTemplate.opsForValue().set(latestMessageKey, chatMessage.getContent());

        String latestMessageTimeKey = String.format(CHAT_ROOM_LATEST_MESSAGE_TIME_KEY, chatRoomId);
        LocalDateTime latestMessageTime = LocalDateTime.now();
        redisTemplate.opsForValue().set(latestMessageTimeKey, latestMessageTime.toString()); // 시간도 저장
    }

    public void broadcastMessage(ChatMessage chatMessage) {

        // 최신 채팅방 정보 생성
        ChatRoom updatedChatRoom = chatRoomRepository.findById(chatMessage.getRoomId())
                .orElseThrow(() -> new BusinessException(Code.CHATROOM_NOT_FOUND));


        ChatRoomDto.chatRoomMessageDTO chatRoomMessageDto = new ChatRoomDto.chatRoomMessageDTO(
                updatedChatRoom.getId(),
                chatMessage.getContent(),
                LocalDateTime.now()
        );

        // 채팅방 참여자들에게 메시지 전송
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), chatMessage);

        // 채팅방 참여자들에게 채팅방 실시간 정렬 전송
        messagingTemplate.convertAndSend("/topic/chatroom/" + chatMessage.getRoomId(), chatRoomMessageDto);


    }


    public List<ChatMessage> getMessagesByChatRoom(Long chatRoomId, Long userId, int page, int size) {
        String chatRoomMessagesKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);

        // 최신 메시지를 기준으로 가져오기 (최근 size개의 메시지만 조회)
        List<Object> messages = redisTemplate.opsForList().range(chatRoomMessagesKey, -size, -1);
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        return messages.stream()
                .map(obj -> (ChatMessage) obj)
                .collect(Collectors.toList());
    }


}
