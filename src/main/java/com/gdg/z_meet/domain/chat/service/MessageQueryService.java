package com.gdg.z_meet.domain.chat.service;

import com.gdg.z_meet.domain.chat.dto.ChatMessage;
import com.gdg.z_meet.domain.chat.entity.Message;
import com.gdg.z_meet.domain.chat.entity.status.MessageType;
import com.gdg.z_meet.domain.chat.repository.JoinChatRepository;
import com.gdg.z_meet.domain.chat.repository.mongo.MongoMessageRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageQueryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoMessageRepository mongoMessageRepository;
    private final JoinChatRepository joinChatRepository;
    private final UserRepository userRepository;

    private static final String CHAT_ROOM_MESSAGES_KEY = "chatroom:%s:messages";

    public List<ChatMessage> getMessagesByChatRoom(Long chatRoomId, Long userId, int page, int size) {
        if (!joinChatRepository.existsByUserIdAndChatRoomIdAndStatusActive(userId, chatRoomId)) {
            throw new BusinessException(Code.JOINCHAT_NOT_FOUND);
        }

        String chatRoomMessagesKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);
        Long totalMessages = redisTemplate.opsForList().size(chatRoomMessagesKey);
        List<ChatMessage> chatMessages = new ArrayList<>();

        // Redis에서 메시지 가져오기 (내림차순)
//        if (totalMessages != null && totalMessages > 0) {
//            int start = (int) Math.max(totalMessages - (page * size) - 1, 0);
//            int end = (int) Math.max(totalMessages - ((page + 1) * size), 0);
//
//            if (start >= end) {
//                List<Object> redisMessages = redisTemplate.opsForList().range(chatRoomMessagesKey, end, start);
//                if (redisMessages != null) {
//                    Collections.reverse(redisMessages); // 최신 메시지가 먼저 오도록
//                    chatMessages = redisMessages.stream()
//                            .map(obj -> (ChatMessage) obj)
//                            .collect(Collectors.toList());
//                }
//            }
//        }

        int redisCount = totalMessages != null ? totalMessages.intValue() : 0;
        int fromIndex = page * size;
        int toIndex = fromIndex + size;

        // 1. Redis에서 가져올 수 있는 부분
        if (fromIndex < redisCount) {
            int redisStart = Math.max(redisCount - toIndex, 0);
            int redisEnd = redisCount - fromIndex - 1;

            List<Object> redisMessages = redisTemplate.opsForList().range(chatRoomMessagesKey, redisStart, redisEnd);
            if (redisMessages != null) {
                Collections.reverse(redisMessages);
                chatMessages = redisMessages.stream()
                        .map(obj -> (ChatMessage) obj)
                        .collect(Collectors.toList());
            }
        }

        int redisFetchedCount = chatMessages.size();
        int remaining = size - redisFetchedCount;

        if (remaining > 0) {
            int mongoOffset = fromIndex - redisCount;
            mongoOffset = Math.max(mongoOffset, 0);

            Pageable pageable = PageRequest.of(mongoOffset / size, remaining, Sort.by("createdAt").descending());
            List<Message> dbMessages = mongoMessageRepository.findByChatRoomId(chatRoomId.toString(), pageable);

            log.info("📌 MongoDB 에서 조회된 메시지 수: {}", dbMessages.size());

            Set<String> redisMessageIds = chatMessages.stream()
                    .map(ChatMessage::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            log.info("📌 Redis 메시지 개수: {}", chatMessages.size());
            log.info("📌 Redis 메시지 UUID 개수: {}", redisMessageIds.size());


            List<ChatMessage> dbChatMessages = dbMessages.stream()
                    .filter(msg -> msg.getMessageId() != null && !redisMessageIds.contains(msg.getMessageId()))
                    .map(message -> {
                        User user = userRepository.findById(Long.parseLong(message.getUserId()))
                                .orElseThrow(() -> new BusinessException(Code.MEMBER_NOT_FOUND));
                        return ChatMessage.builder()
                                .id(message.getMessageId())
                                .type(MessageType.CHAT)
                                .roomId(Long.parseLong(message.getChatRoomId()))
                                .senderId(Long.parseLong(message.getUserId()))
                                .senderName(user.getName())
                                .content(message.getContent())
                                .sendAt(message.getCreatedAt())
                                .emoji(user.getUserProfile().getEmoji())
                                .build();
                    })
                    .collect(Collectors.toList());

            log.info("📌 MongoDB 에서 Redis에 없는 메시지 수: {}", dbChatMessages.size());

            chatMessages.addAll(dbChatMessages);
        }

        return chatMessages.stream()
                .sorted(Comparator.comparing(ChatMessage::getSendAt).reversed())
                .collect(Collectors.toList());
    }
}
