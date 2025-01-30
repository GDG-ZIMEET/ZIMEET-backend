package com.gdg.z_meet.domain.chat.dto;

import com.gdg.z_meet.domain.chat.entity.status.MessageType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {
    private Long chatRoomId;
    private Long senderId;
    private String content;
    private String senderName;
    private MessageType messageType;
}
