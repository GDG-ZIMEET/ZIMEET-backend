package com.gdg.z_meet.domain.chat.repository;

import com.gdg.z_meet.domain.chat.entity.TeamChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamChatRoomRepository extends JpaRepository<TeamChatRoom,Long>{
    List<TeamChatRoom> findByChatRoomId(Long chatRoomId);
}
