package com.gdg.z_meet.domain.meeting.controller;

import com.gdg.z_meet.domain.meeting.converter.MeetingConverter;
import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.service.MeetingQueryService;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.global.common.AuthenticatedUserUtils;
import com.gdg.z_meet.global.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meeting")
@Tag(name = "Meeting")
@Validated
public class MeetingController {

    private final MeetingQueryService meetingQueryService;

    @Operation(summary = "팀 소개 상세 조회", description = "본인 팀은 조회가 불가능합니다.")
    @GetMapping("/{teamId}")
    public Response<MeetingResponseDTO.GetTeamDTO> getTeam(@PathVariable @Positive(message = "팀 ID는 양수여야 합니다.") Long teamId) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        MeetingResponseDTO.GetTeamDTO response = meetingQueryService.getTeam(userId, teamId);

        return Response.ok(response);
    }
}