package com.gdg.z_meet.domain.chat.repository;

import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import com.gdg.z_meet.domain.chat.entity.JoinChat;
import com.gdg.z_meet.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JoinChatRepository extends JpaRepository<JoinChat, Long> {
    List<JoinChat> findByChatRoomId(Long chatRoomId);
    List<JoinChat> findByUserId(Long userId);
    Optional<JoinChat> findByUserAndChatRoom(User user, ChatRoom chatRoom);
    @Query("SELECT uc.user FROM JoinChat uc WHERE uc.chatRoom.id = :chatRoomId")
    List<User> findUsersByChatRoomId(@Param("chatRoomId") Long chatRoomId);

}