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
        List<Object> redisObjects = redisTemplate.opsForList().range(chatRoomMessagesKey, 0, -1);
        List<ChatMessage> redisMessages = Optional.ofNullable(redisObjects).orElse(Collections.emptyList()).stream()
                .map(obj -> (ChatMessage) obj)
                .collect(Collectors.toList());

        Pageable pageable = Pageable.unpaged(); // 전체 다 가져오기
        List<Message> dbMessages = mongoMessageRepository.findByChatRoomId(chatRoomId.toString(), pageable);

        List<ChatMessage> mongoMessages = dbMessages.stream()
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

        // 중복 제거
        Set<String> redisIds = redisMessages.stream()
                .map(ChatMessage::getId)
                .collect(Collectors.toSet());

        List<ChatMessage> combined = new ArrayList<>();
        combined.addAll(redisMessages);
        combined.addAll(mongoMessages.stream()
                .filter(msg -> !redisIds.contains(msg.getId()))
                .collect(Collectors.toList()));

        // 최신순 정렬
        combined.sort(Comparator.comparing(ChatMessage::getSendAt).reversed());

        // 정확한 페이지 잘라서 리턴
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, combined.size());

        if (fromIndex >= combined.size()) {
            return Collections.emptyList();
        }

        return combined.subList(fromIndex, toIndex);
    }


}
