package com.gdg.z_meet.domain.chat.service;

import com.gdg.z_meet.domain.chat.dto.ChatMessage;
import com.gdg.z_meet.domain.chat.entity.Message;
import com.gdg.z_meet.domain.chat.entity.status.MessageType;
import com.gdg.z_meet.domain.chat.repository.JoinChatRepository;
import com.gdg.z_meet.domain.chat.repository.MessageRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageQueryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageRepository messageRepository;
    private final JoinChatRepository joinChatRepository;

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
                            .collect(Collectors.toList());
                }
            }
        }

        // Redis에서 가져온 메시지가 부족하면 DB에서 추가로 가져오기
        if (chatMessages.size() < size) {
            Pageable pageable = PageRequest.of(page, size - chatMessages.size(), Sort.by("createdAt").descending());
            List<Message> dbMessages = messageRepository.findByChatRoomId(chatRoomId, pageable);
            List<ChatMessage> dbChatMessages = dbMessages.stream()
                    .map(message ->  ChatMessage.builder()
                            .type(MessageType.CHAT)
                            .roomId(message.getChatRoom().getId())
                            .senderId(message.getUser().getId())
                            .senderName(message.getUser().getName())
                            .content(message.getContent())
                            .sendAt(message.getCreatedAt())
                            .emoji(message.getUser().getUserProfile().getEmoji())
                            .build())
                    .collect(Collectors.toList());


            chatMessages.addAll(dbChatMessages);
        }

        return chatMessages;
    }
}
