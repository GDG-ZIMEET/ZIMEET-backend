package com.gdg.z_meet.domain.chat.service;

import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import com.gdg.z_meet.domain.chat.repository.ChatRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;

    private static final String CHAT_ROOMS_KEY = "chatrooms";
    private static final String CHAT_ROOM_ACTIVITY_KEY = "chatroom:activity";
    private static final String CHAT_ROOM_LATEST_MESSAGE_KEY = "chatroom:%s:latestMessage";

    // 채팅방 생성
    @Transactional
    public ChatRoom createChatRoom(String name) {
        // 1. 새로운 ChatRoom 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(name)
                .build();

        // 2. DB 저장
        chatRoom = chatRoomRepository.save(chatRoom);

        // 3. Redis 저장
        redisTemplate.opsForHash().put(CHAT_ROOMS_KEY, chatRoom.getId().toString(), chatRoom);

        return chatRoom;
    }
}
