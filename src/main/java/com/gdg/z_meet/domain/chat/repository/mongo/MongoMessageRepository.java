package com.gdg.z_meet.domain.chat.repository.mongo;

import com.gdg.z_meet.domain.chat.dto.ChatMessage;
import com.gdg.z_meet.domain.chat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoMessageRepository extends MongoRepository<Message, String> {
    // MongoDB에서 String 타입의 chatRoomId를 받아 메시지를 조회
    List<Message> findByChatRoomId(String chatRoomId, Pageable pageable);
    List<Message> findByUserId(String userId);
    void deleteByUserId(String userId);
}
