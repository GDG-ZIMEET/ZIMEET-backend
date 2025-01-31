package com.gdg.z_meet.domain.chat.repository;

import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("SELECT ucr.chatRoom FROM JoinChat ucr WHERE ucr.user.id = :userId")
    List<ChatRoom> findChatRoomsByUserId(Long userId);
}

