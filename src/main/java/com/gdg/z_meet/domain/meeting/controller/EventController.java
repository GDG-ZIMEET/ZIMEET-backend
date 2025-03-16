package com.gdg.z_meet.domain.meeting.controller;

import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.service.MeetingCommandService;
import com.gdg.z_meet.global.common.AuthenticatedUserUtils;
import com.gdg.z_meet.global.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/event")
@Tag(name = "Event")
@Validated
public class EventController {

    private final MeetingCommandService meetingCommandService;

    @Operation(summary = "팀 삭제 기회 추가")
    @PatchMapping("/leftDelete")
    public Response<MeetingResponseDTO.GetMyDeleteDTO> getMyDelete(@RequestParam(name = "name") String name, @RequestParam(name = "phoneNumber") String phoneNumber) {

        MeetingResponseDTO.GetMyDeleteDTO response = meetingCommandService.patchMyDelete(name, phoneNumber);

        return Response.ok(response);
    }
}
