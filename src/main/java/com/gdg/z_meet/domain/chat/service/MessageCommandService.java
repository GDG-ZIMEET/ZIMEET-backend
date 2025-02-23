package com.gdg.z_meet.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gdg.z_meet.domain.chat.dto.ChatMessage;
import com.gdg.z_meet.domain.chat.dto.ChatRoomDto;
import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import com.gdg.z_meet.domain.chat.entity.Message;
import com.gdg.z_meet.domain.chat.repository.ChatRoomRepository;
import com.gdg.z_meet.domain.chat.repository.JoinChatRepository;
import com.gdg.z_meet.domain.chat.repository.MessageRepository;
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
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MessageCommandService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String CHAT_ROOM_MESSAGES_KEY = "chatroom:%s:messages";
    private static final String CHAT_ROOM_LATEST_MESSAGE_KEY = "chatroom:%s:latestMessage";
    private static final String CHAT_ROOM_LATEST_MESSAGE_TIME_KEY = "chatroom:%s:latestMessageTime";
    private static final int MAX_REDIS_MESSAGES = 100; // 최신 100개만 Redis에 유지

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
        System.out.println("✅ Redis → DB 저장 시작");

        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        for (ChatRoom chatRoom : chatRooms) {
            Long chatRoomId = chatRoom.getId();
            String chatRoomMessagesKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);
            Long totalMessages = redisTemplate.opsForList().size(chatRoomMessagesKey);

            if (totalMessages == null || totalMessages <= MAX_REDIS_MESSAGES) {
                continue; // 최신 100개 이하라면 저장하지 않음
            }

            // 최신 100개를 유지하고 나머지 메시지를 DB에 저장
            int messagesToMove = (int) (totalMessages - MAX_REDIS_MESSAGES);
            if (messagesToMove <= 0) continue; // 저장할 메시지가 없음

            List<Object> messages = redisTemplate.opsForList().range(chatRoomMessagesKey, 0, messagesToMove - 1);
            if (messages != null && !messages.isEmpty()) {
                List<ChatMessage> chatMessages = messages.stream()
                        .map(obj -> objectMapper.convertValue(obj, ChatMessage.class)) // ✅ 안전한 변환
                        .filter(chatMessage -> chatMessage.getSenderId() != null)
                        .peek(chatMessage -> System.out.println("Processing ChatMessage: " + chatMessage))
                        .collect(Collectors.toList());

                List<Message> messageList = chatMessages.stream()
                        .map(chatMessage -> Message.builder()
                                .chatRoom(chatRoom)
                                .user(userRepository.findById(chatMessage.getSenderId())
                                        .orElseThrow(() -> new BusinessException(Code.MEMBER_NOT_FOUND)))
                                .content(chatMessage.getContent())
                                .build())
                        .collect(Collectors.toList());

                messageRepository.saveAll(messageList);

                // 저장한 메시지를 Redis에서 삭제 (앞에서부터 messagesToMove개 삭제)
                redisTemplate.opsForList().trim(chatRoomMessagesKey, messagesToMove, -1);
            }
        }

        System.out.println("✅ Redis → DB 저장 완료");
    }


}
