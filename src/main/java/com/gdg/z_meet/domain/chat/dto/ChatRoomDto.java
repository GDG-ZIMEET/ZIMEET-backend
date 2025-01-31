package com.gdg.z_meet.domain.chat.dto;

import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import com.gdg.z_meet.domain.user.entity.enums.Emoji;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

public class ChatRoomDto {

    @Getter
    @AllArgsConstructor
    @Builder
    public static class resultChatRoomDto{
        private Long id;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class chatRoomListDto{
        private Long chatRoomId;
        private String chatRoomName;
        private String latestMessage;    //최신 메시지
        private LocalDateTime lastestTime; //시간
        private List<UserProfileDto> userProfiles; // 사용자 프로필 목록
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class UserProfileDto {
        private Long id;
        private String name;
        private Emoji emoji;
    }


}
