package com.gdg.z_meet.domain.chat.dto;

import com.gdg.z_meet.domain.chat.entity.status.MessageType;
import com.gdg.z_meet.domain.user.entity.enums.Emoji;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MessageDto {
    private Long chatRoomId;
    private Long senderId;
    private String content;
    private String senderName;
    private Emoji emoji;
    private MessageType messageType;
}
