package com.gdg.z_meet.domain.chat.dto;

import lombok.*;

public class ChatRoomDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class resultChatRoomDto{
        private Long id;
        private String name;
    }
}
