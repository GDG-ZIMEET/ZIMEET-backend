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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageQueryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoMessageRepository mongoMessageRepository;
    private final JoinChatRepository joinChatRepository;
    private final UserRepository userRepository;

    private static final String CHAT_ROOM_MESSAGES_KEY = "chatroom:%s:messages";

    public List<ChatMessage> getMessagesByChatRoom(Long chatRoomId, Long userId, int page, int size) {
        if (!joinChatRepository.existsByUserIdAndChatRoomId(userId, chatRoomId)) {
            throw new BusinessException(Code.JOINCHAT_NOT_FOUND);
        }

        String chatRoomMessagesKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);
        Long totalMessages = redisTemplate.opsForList().size(chatRoomMessagesKey);
        List<ChatMessage> chatMessages = new ArrayList<>();

        // Redis에서 메시지 가져오기
        if (totalMessages != null && totalMessages > 0) {
            int start = (int) Math.max(totalMessages - ((page + 1) * size), 0);
            int end = (int) (totalMessages - (page * size) - 1);

            if (start <= end) {
                List<Object> redisMessages = redisTemplate.opsForList().range(chatRoomMessagesKey, start, end);
                if (redisMessages != null) {
                    chatMessages = redisMessages.stream()
                            .map(obj -> (ChatMessage) obj)
                            .sorted(Comparator.comparing(ChatMessage::getSendAt)) //오름차순 정렬
                            .collect(Collectors.toList());
                }
            }
        }

        // Redis에서 가져온 메시지가 부족하면 DB에서 추가로 가져오기
        if (chatMessages.size() < size) {
            Pageable pageable = PageRequest.of(page, size - chatMessages.size(), Sort.by("createdAt").descending());
            List<Message> dbMessages = mongoMessageRepository.findByChatRoomId(String.valueOf(chatRoomId), pageable);

            List<ChatMessage> dbChatMessages = dbMessages.stream()
                    .map(message -> {
                        // MySQL에서 userId를 기반으로 User 객체를 조회
                        User user = userRepository.findById(Long.parseLong(message.getUserId()))
                                .orElseThrow(() -> new BusinessException(Code.MEMBER_NOT_FOUND));


                        return ChatMessage.builder()
                                .type(MessageType.CHAT)
                                .roomId(Long.parseLong(message.getChatRoomId()))  // MongoDB의 chatRoomId는 String이므로 Long으로 변환
                                .senderId(Long.parseLong(message.getUserId()))  // MongoDB의 userId는 String이므로 Long으로 변환
                                .senderName(user.getName())  // MySQL에서 가져온 user의 name 사용
                                .content(message.getContent())
                                .sendAt(message.getCreatedAt())
                                .emoji(user.getUserProfile().getEmoji())  // MySQL에서 가져온 user의 emoji 사용
                                .build();
                    })
                    .sorted(Comparator.comparing(ChatMessage::getSendAt))
                    .collect(Collectors.toList());

            chatMessages.addAll(dbChatMessages);
        }

        chatMessages = chatMessages.stream()
                .sorted(Comparator.comparing(ChatMessage::getSendAt)) //오름차순 정렬
                .collect(Collectors.toList());

        return chatMessages;
    }
}
