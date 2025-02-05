package com.gdg.z_meet.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gdg.z_meet.domain.chat.entity.status.MessageType;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage implements Serializable {
    private String id;
    private MessageType type;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime sendAt;
    private String emoji;

}