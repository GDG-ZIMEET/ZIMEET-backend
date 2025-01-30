package com.gdg.z_meet.domain.chat.controller;

import com.gdg.z_meet.domain.chat.converter.ChatRoomConverter;
import com.gdg.z_meet.domain.chat.dto.ChatRoomDto;
import com.gdg.z_meet.domain.chat.entity.ChatRoom;
import com.gdg.z_meet.domain.chat.service.ChatRoomService;
import com.gdg.z_meet.domain.chat.service.MessageService;
import com.gdg.z_meet.global.jwt.JwtUtil;
import com.gdg.z_meet.global.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat") // API 기본 URL 설정
@RequiredArgsConstructor
@Tag(name = "ChatAPI",description = "채팅방 관련 기능 API 입니다.")
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

    @Operation(summary = "사용자 채팅방 추가", description = "관리자가 사용자를 지정된 채팅방에 추가합니다.")
    @PostMapping("/rooms/{roomId}/users")
    public Response<String> addUserToChatRoom(
            @PathVariable Long roomId,
            @RequestParam Long userId) {
        chatRoomService.addUserToChatRoom(roomId, userId); // 채팅방에 사용자 추가
        return Response.ok(roomId+" 추가 완료되었습니다."); // 추가 성공 응답 반환
    }


    @Operation(summary = "사용자 채팅방 제거", description = "사용자를 지정된 채팅방에서 제거합니다. 채팅방 나가기와 동일한 기능 입니다. ")
    @DeleteMapping("/rooms/{roomId}/users")
    public Response<String> removeUserFromChatRoom(
            @RequestHeader("Authorization") String token,
            @PathVariable Long roomId) {

        // 토큰에서 사용자 ID 추출
        Long userId = extractUserIdFromToken(token);
        System.out.println("UserID: "+userId);

        // 채팅방에서 사용자 제거
        chatRoomService.removeUserFromChatRoom(roomId, userId);

        return Response.ok(roomId+" 삭제 완료되었습니다.");
    }

    @Operation(summary = "사용자 참여 채팅방 조회", description = "사용자가 현재 참여 중인 채팅방 목록을 조회합니다.")
    @GetMapping("/users/rooms")
    public ResponseEntity<List<ChatRoomDto.chatRoomListDto>> getUserChatRooms(
            @RequestHeader("Authorization") String token) {
        Long userId = extractUserIdFromToken(token); // JWT 토큰에서 사용자 ID 추출
        List<ChatRoomDto.chatRoomListDto> chatRooms = chatRoomService.getChatRoomsByUser(userId); // 참여 중인 채팅방 조회
        return ResponseEntity.ok(chatRooms); // 채팅방 목록 반환
    }


    private Long extractUserIdFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7); // "Bearer " 제거
        }

        return  jwtUtil.getUserIdFromToken(token); // 토큰에서 사용자 ID 추출
    }

}
