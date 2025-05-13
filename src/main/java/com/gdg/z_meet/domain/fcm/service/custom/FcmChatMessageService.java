package com.gdg.z_meet.domain.fcm.service.custom;

import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import com.gdg.z_meet.domain.chat.entity.JoinChat;
import com.gdg.z_meet.domain.chat.entity.TeamChatRoom;
import com.gdg.z_meet.domain.chat.repository.ChatRoomRepository;
import com.gdg.z_meet.domain.chat.repository.JoinChatRepository;
import com.gdg.z_meet.domain.chat.repository.TeamChatRoomRepository;
import com.gdg.z_meet.domain.fcm.service.FcmMessageClient;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gdg.z_meet.global.response.Code;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class FcmChatMessageService {

    private final FcmMessageClient fcmMessageClient;
    private final ChatRoomRepository chatRoomRepository;
    private final TeamChatRoomRepository teamChatRoomRepository;
    private final JoinChatRepository joinChatRepository;


    // 1. (1대1) 상대 닉네임 / (2대2) 상대 팀 / (랜덤) 채팅방 이름
    //    본문 : 채팅 메시지
    @Transactional
    public void messagingChat() {


        String title = "";
        String body = "";

        try {
            fcmMessageClient.sendFcmMessage(user.getId(), title, body);
        } catch (Exception e) {
            log.error("FCM 채팅 메시지 전송 실패 - userId: {}, error: {}", user.getId(), e.getMessage(), e);
        }
    }


    // 2. [ (1대1) 상대 닉네임 / (2대2) 상대 팀 / (랜덤) 채팅방 이름 ] 채팅방이 열렸어요! 🤗
    @Transactional
    public void messagingOpenChatRoom(User user, Long roomId) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(Code.CHATROOM_NOT_FOUND));

        String title = "";
        switch (chatRoom.getChatType()) {
            case USER: {
                List<JoinChat> joinChats = joinChatRepository.findByChatRoomId(chatRoom.getId());

                User other = joinChats.stream()
                        .map(JoinChat::getUser)
                        .filter(u -> !u.getId().equals(user.getId()))
                        .findFirst()
                        .orElse(null);

                title = (other != null)
                        ? other.getUserProfile().getNickname() + " 님과의 채팅방이 열렸어요! 🤗"
                        : "채팅방이 열렸어요! 🤗";
                break;
            }

            case TEAM: {
                Team otherTeam = teamChatRoomRepository
                        .findOtherTeamInChatRoom(chatRoom.getId(), user.getId())
                        .orElse(null);

                title = (otherTeam != null)
                        ? otherTeam.getName()  + " 팀과의 채팅방이 열렸어요! 🤗"
                        : "채팅방이 열렸어요! 🤗";
                break;
            }

            case RANDOM: {
                TeamChatRoom otherteamChatRoom = teamChatRoomRepository.findFirstByChatRoomId(chatRoom.getId())
                        .orElseThrow(() -> new BusinessException(Code.CHATROOM_NOT_FOUND));
                title = (otherteamChatRoom != null)
                        ? otherteamChatRoom.getName() + " 채팅방이 열렸어요! 🤗"
                        : "채팅방이 열렸어요! 🤗";
                break;
            }
        }
            String body = "두근두근💗 새로운 사람들과 인사부터 시작해보세요!";
        try {
            fcmMessageClient.sendFcmMessage(user.getId(), title, body);
        } catch (Exception e) {
            log.error("FCM 채팅방 열림 관련 메시지 전송 실패 - userId: {}, error: {}", user.getId(), e.getMessage(), e);
        }
    }
}
