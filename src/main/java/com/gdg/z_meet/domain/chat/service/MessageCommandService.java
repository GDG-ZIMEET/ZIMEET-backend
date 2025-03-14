package com.gdg.z_meet.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gdg.z_meet.domain.chat.dto.ChatMessage;
import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import com.gdg.z_meet.domain.chat.entity.Message;
import com.gdg.z_meet.domain.chat.repository.ChatRoomRepository;
import com.gdg.z_meet.domain.chat.repository.mongo.MongoMessageRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MessageCommandService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MongoMessageRepository mongoMessageRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String CHAT_ROOM_MESSAGES_KEY = "chatroom:%s:messages";
    private static final String CHAT_ROOM_LATEST_MESSAGE_KEY = "chatroom:%s:latestMessage";
    private static final String CHAT_ROOM_LATEST_MESSAGE_TIME_KEY = "chatroom:%s:latestMessageTime";
    private static final int MAX_REDIS_MESSAGES = 30; // 최신 30개만 Redis에 유지

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
        // 채팅방 참여자들에게 메시지 전송
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), chatMessage);

    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void saveMessagesToDB() {

        List<ChatRoom> chatRooms = chatRoomRepository.findAll();  // MySQL에서 chatRoom 조회
        for (ChatRoom chatRoom : chatRooms) {
            Long chatRoomId = chatRoom.getId();
            String chatRoomMessagesKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);
            Long totalMessages = redisTemplate.opsForList().size(chatRoomMessagesKey);

            if (totalMessages == null || totalMessages <= MAX_REDIS_MESSAGES) {
                continue;  // MAX개 이하라면 저장하지 않음
            }

            int messagesToMove = (int) (totalMessages - MAX_REDIS_MESSAGES);
            if (messagesToMove <= 0) continue;  // 저장할 메시지가 없음

            List<Object> messages = redisTemplate.opsForList().range(chatRoomMessagesKey, 0, messagesToMove - 1);
            if (messages != null && !messages.isEmpty()) {
                List<ChatMessage> chatMessages = messages.stream()
                        .map(obj -> objectMapper.convertValue(obj, ChatMessage.class))
                        .filter(chatMessage -> chatMessage.getSenderId() != null)
                        .collect(Collectors.toList());

                List<Message> messageList = chatMessages.stream()
                        .map(chatMessage -> {
                            // MySQL에서 userId, chatRoomId를 가져와 MongoDB에 저장
                            User user = userRepository.findById(chatMessage.getSenderId())
                                    .orElseThrow(() -> new BusinessException(Code.MEMBER_NOT_FOUND));
                            LocalDateTime now = LocalDateTime.now();

                            return Message.builder()
                                    .chatRoomId(chatRoom.getId().toString())
                                    .userId(user.getId().toString())
                                    .content(chatMessage.getContent())
                                    .createdAt(chatMessage.getSendAt())
                                    .updatedAt(now)
                                    .build();
                        })
                        .collect(Collectors.toList());
                mongoMessageRepository.saveAll(messageList);

                // 저장한 메시지를 Redis에서 삭제
                redisTemplate.opsForList().trim(chatRoomMessagesKey, messagesToMove, -1);
            }
        }

    }



}
