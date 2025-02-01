package com.gdg.z_meet.domain.chat.service;

import com.gdg.z_meet.domain.chat.dto.ChatMessage;
import com.gdg.z_meet.domain.chat.dto.ChatRoomDto;
import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import com.gdg.z_meet.domain.chat.entity.status.MessageType;
import com.gdg.z_meet.domain.chat.repository.ChatRoomRepository;
import com.gdg.z_meet.domain.chat.repository.MessageRepository;
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


@Service
@RequiredArgsConstructor
public class MessageService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String CHAT_ROOM_MESSAGES_KEY = "chatroom:%s:messages"; // 메시지 리스트
    private static final String CHAT_ROOM_ACTIVITY_KEY = "chatroom:activity"; // 채팅방 활동 시간
    private static final String CHAT_ROOM_LATEST_MESSAGE_KEY = "chatroom:%s:latestMessage"; // 최신 메시지
    private static final String CHAT_ROOM_MESSAGE_READERS_KEY = "chatroom:%s:message:%s:readUsers"; // 메시지 읽은 사용자 리스트

    @Transactional
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        Long chatRoomId = Long.parseLong(chatMessage.getRoomId());

        // Redis에 메시지 저장
        String chatRoomMessagesKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);
        redisTemplate.opsForList().rightPush(chatRoomMessagesKey, chatMessage);

        // 최신 메시지 및 활동 시간 업데이트
        String latestMessageKey = String.format(CHAT_ROOM_LATEST_MESSAGE_KEY, chatRoomId);
        redisTemplate.opsForValue().set(latestMessageKey, chatMessage.getContent());

        String latestMessageTimeKey = String.format("chatroom:%s:latestMessageTime", chatRoomId);
        LocalDateTime latestMessageTime = LocalDateTime.now();
        redisTemplate.opsForValue().set(latestMessageTimeKey, latestMessageTime.toString()); // 시간도 저장

        // ✅ 새로운 채팅방 정보를 생성하여 클라이언트에 브로드캐스트
        ChatRoom updatedChatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(Code.CHATROOM_NOT_FOUND));


        List<ChatRoomDto.UserProfileDto> userProfiles = chatRoomService.getUserProfilesByChatRoomId(chatRoomId);

        ChatRoomDto.chatRoomListDto chatRoomDto = new ChatRoomDto.chatRoomListDto(
                updatedChatRoom.getId(),
                updatedChatRoom.getName(),
                chatMessage.getContent(),      // 최신 메시지
                latestMessageTime,             // 최신 메시지 시간
                userProfiles                   // 사용자 프로필 목록
        );

        messagingTemplate.convertAndSend("/topic/chatrooms", chatRoomDto);

        return chatMessage;
    }


}