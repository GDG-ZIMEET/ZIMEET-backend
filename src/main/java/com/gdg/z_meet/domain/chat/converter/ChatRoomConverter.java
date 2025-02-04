package com.gdg.z_meet.domain.chat.converter;

import com.gdg.z_meet.domain.chat.dto.ChatRoomDto;
import com.gdg.z_meet.domain.chat.entity.ChatRoom;

public class ChatRoomConverter {

    public static ChatRoomDto.resultChatRoomDto chatRoomtoResultDto(ChatRoom chatRoom){
        return ChatRoomDto.resultChatRoomDto.builder()
                .id(chatRoom.getId())
                .build();
    }
}
