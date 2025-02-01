package com.gdg.z_meet.domain.chat.service;

import com.gdg.z_meet.domain.chat.dto.ChatMessage;
import com.gdg.z_meet.domain.chat.entity.status.MessageType;
import com.gdg.z_meet.domain.chat.repository.MessageRepository;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageRepository messageRepository;
    @Autowired
    private final ChatRoomService chatRoomService;
    private final UserService userService;

    private static final String CHAT_ROOM_MESSAGES_KEY = "chatroom:%s:messages"; // 메시지 리스트
    private static final String CHAT_ROOM_ACTIVITY_KEY = "chatroom:activity"; // 채팅방 활동 시간
    private static final String CHAT_ROOM_LATEST_MESSAGE_KEY = "chatroom:%s:latestMessage"; // 최신 메시지
    private static final String CHAT_ROOM_MESSAGE_READERS_KEY = "chatroom:%s:message:%s:readUsers"; // 메시지 읽은 사용자 리스트
    @Autowired
    private UserRepository userRepository;

    // 메시지 저장
    @Transactional
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        Long chatRoomId = Long.parseLong(chatMessage.getRoomId());

        // Redis에 메시지 저장
        String chatRoomMessagesKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);
        redisTemplate.opsForList().rightPush(chatRoomMessagesKey, chatMessage);

        // 최신 메시지 및 활동 시간 업데이트
        String latestMessageKey = String.format(CHAT_ROOM_LATEST_MESSAGE_KEY, chatRoomId);
        redisTemplate.opsForValue().set(latestMessageKey, chatMessage.getContent());
        redisTemplate.opsForZSet().add(CHAT_ROOM_ACTIVITY_KEY, chatRoomId.toString(), System.currentTimeMillis());

        System.out.println("Fetching messages from Redis:");
        System.out.println("Key: " + chatRoomMessagesKey);


        return chatMessage;
    }

}