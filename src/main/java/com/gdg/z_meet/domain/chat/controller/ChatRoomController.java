package com.gdg.z_meet.domain.chat.controller;

import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import com.gdg.z_meet.domain.chat.service.ChatRoomService;
import com.gdg.z_meet.domain.chat.service.MessageService;
import com.gdg.z_meet.global.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat") // API 기본 URL 설정
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService; // 채팅방 관리 서비스
    private final MessageService messageService; // 메시지 관리 서비스
    private final JwtUtil jwtUtil;

    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoom> createChatRoom(
            @RequestParam String name) {
        return ResponseEntity.ok(chatRoomService.createChatRoom(name)); // 생성된 채팅방 반환
    }

}
