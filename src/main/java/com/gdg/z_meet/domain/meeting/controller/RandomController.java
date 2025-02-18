package com.gdg.z_meet.domain.meeting.controller;

import com.gdg.z_meet.domain.meeting.dto.MeetingRequestDTO;
import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.dto.RandomResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.TeamType;
import com.gdg.z_meet.domain.meeting.service.MeetingCommandService;
import com.gdg.z_meet.domain.meeting.service.MeetingQueryService;
import com.gdg.z_meet.global.common.AuthenticatedUserUtils;
import com.gdg.z_meet.global.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/random")
@Tag(name = "RandomMeeting")
@Validated
public class RandomController {

    private final MeetingQueryService meetingQueryService;
    private final MeetingCommandService meetingCommandService;

    @Operation(summary = "남은 티켓 개수")
    @GetMapping("/ticket")
    public Response<RandomResponseDTO.GetTicketDTO> getMyDelete() {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        RandomResponseDTO.GetTicketDTO response;

        return Response.ok(response);
    }
}