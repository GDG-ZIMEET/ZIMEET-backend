package com.gdg.z_meet.domain.meeting.controller;

import com.gdg.z_meet.domain.meeting.converter.MeetingConverter;
import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.service.MeetingQueryService;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.global.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/meeting")
@Validated
public class MeetingController {

    private final MeetingQueryService meetingQueryService;

    @Operation(summary = "팀 상세 조회")
    @GetMapping("/detail/{teamId}")
    public Response<MeetingResponseDTO.GetTeamDTO> getTeam(@PathVariable Long teamId, @RequestParam Long userId) {

        Team team = meetingQueryService.getTeam(userId, teamId);
        List<User> users = meetingQueryService.getUserTeam(teamId);
        return Response.ok(MeetingConverter.toGetTeamDTO(team, users));
    }
}