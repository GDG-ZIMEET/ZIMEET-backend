package com.gdg.z_meet.domain.chat.controller;

import com.gdg.z_meet.domain.chat.converter.ChatRoomConverter;
import com.gdg.z_meet.domain.chat.dto.ChatRoomDto;
import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import com.gdg.z_meet.domain.chat.service.ChatRoomService;
import com.gdg.z_meet.domain.chat.service.MessageService;
import com.gdg.z_meet.global.jwt.JwtUtil;
import com.gdg.z_meet.global.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat") // API 기본 URL 설정
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    @PostMapping("/rooms")
    public Response<ChatRoomDto.resultChatRoomDto> createChatRoom(
            @RequestParam String name) {
        ChatRoom chatRoom = chatRoomService.createChatRoom(name);
        return Response.ok(ChatRoomConverter.chatRoomtoResultDto(chatRoom)); // 생성된 채팅방 반환
    }

    @Operation(summary = "채팅방 삭제", description = "기존 채팅방을 삭제합니다.")
    @DeleteMapping("/rooms/{roomId}")
    public Response<String> deleteChatRoom(
            @PathVariable Long roomId) {
        chatRoomService.deleteChatRoom(roomId); // 채팅방 삭제
        return Response.ok(roomId+" 삭제 완료되었습니다.");
    }

}
