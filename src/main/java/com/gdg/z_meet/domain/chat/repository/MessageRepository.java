package com.gdg.z_meet.domain.chat.repository;

import com.gdg.z_meet.domain.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChatRoomId(Long chatRoomId, Pageable pageable);  // Pageable 사용
    List<Message> findByChatRoomId(Long chatRoomId);  // 전체 조회
}
