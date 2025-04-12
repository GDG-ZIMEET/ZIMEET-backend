package com.gdg.z_meet.domain.chat.controller;

import com.gdg.z_meet.domain.chat.dto.ChatMessage;
import com.gdg.z_meet.domain.chat.dto.ChatRoomDto;
import com.gdg.z_meet.domain.chat.service.*;
import com.gdg.z_meet.domain.meeting.dto.MeetingRequestDTO;
import com.gdg.z_meet.global.common.AuthenticatedUserUtils;
import com.gdg.z_meet.global.jwt.JwtUtil;
import com.gdg.z_meet.global.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms") // API 기본 URL 설정
@RequiredArgsConstructor
@Tag(name = "ChatAPI",description = "채팅방 관련 기능 API 입니다.")
public class ChatRoomController {

    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatRoomCommandService chatRoomCommandService;
    private final MessageQueryService messageQueryService;

    @Operation(summary = "채팅방 삭제", description = "기존 채팅방을 삭제합니다.")
    @DeleteMapping("/{roomId}")
    public Response<String> deleteChatRoom(
            @PathVariable Long roomId) {
        chatRoomCommandService.deleteChatRoom(roomId); // 채팅방 삭제
        return Response.ok(roomId+" 삭제 완료되었습니다.");
    }

    @Operation(summary = "팀 채팅방 추가/하이 수락하기", description = "관리자가 팀을 채팅방에 추가합니다. 추가할 팀 아이디를 주세요")
    @PostMapping("/teams")
    public Response<ChatRoomDto.resultChatRoomDto> addUserToChatRoom(
            @RequestBody MeetingRequestDTO.hiDto hiDto) {
        return Response.ok(chatRoomCommandService.addTeamJoinChat(hiDto));
    }

    @Operation(summary = "(랜덤채팅 전용) 사용자 채팅방 추가", description = "관리자가 사용자를 채팅방에 추가합니다. 추가할 사용자 아이디들을 주세요")
    @PostMapping("/addUsers")
    public Response<ChatRoomDto.resultChatRoomDto> addUserToChatRoom(
            @RequestBody List<Long> userIds) {
        return Response.ok(chatRoomCommandService.addUserJoinChat(userIds));
    }

    @Operation(summary = "사용자 채팅방 제거", description = "사용자를 지정된 채팅방에서 제거합니다. 채팅방 나가기와 동일한 기능 입니다. ")
    @DeleteMapping("/{roomId}/users")
    public Response<String> removeUserFromChatRoom(
            @PathVariable Long roomId) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();

        // 채팅방에서 사용자 제거
        chatRoomCommandService.removeUserFromChatRoom(roomId, userId);

        return Response.ok(roomId+" 삭제 완료되었습니다.");
    }

    @Operation(summary = "사용자 참여 채팅방 조회", description = "사용자가 현재 참여 중인 채팅방 목록을 조회합니다.")
    @GetMapping("/users")
    public Response<List<ChatRoomDto.chatRoomListDto>> getUserChatRooms() {
        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        List<ChatRoomDto.chatRoomListDto> chatRooms = chatRoomQueryService.getChatRoomsByUser(userId); // 참여 중인 채팅방 조회
        return Response.ok(chatRooms); // 채팅방 목록 반환
    }

    @Operation(summary = "채팅방 사용자 조회 ", description = "특정 채팅방에 있는 사용자들을 조회합니다. ")
    @GetMapping("/{roomId}")
    public Response<List<ChatRoomDto.chatRoomUserList>> sendMessage(
            @PathVariable Long roomId) {
        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        return Response.ok(chatRoomQueryService.getUserByRoomId(userId, roomId)); // 성공 응답 반환
    }

    @Operation(summary = "메시지 조회", description = "지정된 채팅방의 메시지를 페이지네이션을 사용하여 조회합니다.")
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page, // 페이지 번호 (기본값: 0)
            @RequestParam(defaultValue = "15") int size // 페이지 크기 (기본값: 20)
    ) {
        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        List<ChatMessage> messages = messageQueryService.getMessagesByChatRoom(roomId,userId, page, size);
        return ResponseEntity.ok(messages);
    }

}
