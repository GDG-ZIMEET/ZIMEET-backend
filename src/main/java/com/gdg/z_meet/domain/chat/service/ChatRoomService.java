package com.gdg.z_meet.domain.chat.service;

import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import com.gdg.z_meet.domain.chat.entity.JoinChat;
import com.gdg.z_meet.domain.chat.entity.Message;
import com.gdg.z_meet.domain.chat.repository.ChatRoomRepository;
import com.gdg.z_meet.domain.chat.repository.JoinChatRepository;
import com.gdg.z_meet.domain.chat.repository.MessageRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.h2.api.ErrorCode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final JoinChatRepository joinChatRepository;

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

    // 채팅방 삭제
    @Transactional
    public void deleteChatRoom(Long chatRoomId) {
        // 채팅방 존재 여부 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(Code.CHATROOM_NOT_FOUND));

        // 연관된 메시지 삭제
        List<Message> messages = messageRepository.findByChatRoomId(chatRoomId);
        if (!messages.isEmpty()) {
            messageRepository.deleteAll(messages);
        }

        // 연관된 JoinChat 삭제
        List<JoinChat> joinChats = joinChatRepository.findByChatRoomId(chatRoomId);
        if (!joinChats.isEmpty()) {
            joinChats.forEach(joinChat -> {
                String joinChatsKey = "user:" + joinChat.getUser().getId() + ":chatrooms";
                redisTemplate.opsForSet().remove(joinChatsKey, chatRoomId);
            });
            joinChatRepository.deleteAll(joinChats);
        }

        // Redis에서 채팅방 삭제
        redisTemplate.opsForHash().delete(CHAT_ROOMS_KEY, chatRoomId.toString());

        // Redis의 활동 시간 데이터 삭제
        redisTemplate.opsForZSet().remove(CHAT_ROOM_ACTIVITY_KEY, chatRoomId.toString());

        // 채팅방 삭제
        chatRoomRepository.deleteById(chatRoomId);

    }

}
