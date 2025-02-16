package com.gdg.z_meet.domain.meeting.controller;

import com.gdg.z_meet.domain.meeting.dto.MeetingRequestDTO;
import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meeting")
@Tag(name = "Meeting")
@Validated
public class MeetingController {

    private final MeetingQueryService meetingQueryService;
    private final MeetingCommandService meetingCommandService;

    @Operation(summary = "팀 갤러리 조회", description = "12팀씩 페이징 됩니다.")
    @GetMapping
    public Response<MeetingResponseDTO.GetTeamGalleryDTO> getTeamGallery(@RequestParam TeamType teamType, @RequestParam Integer page) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        MeetingResponseDTO.GetTeamGalleryDTO response = meetingQueryService.getTeamGallery(userId, teamType, page);

        return Response.ok(response);
    }

    @Operation(summary = "팀 소개 상세 조회", description = "본인 팀은 조회가 불가능합니다.")
    @GetMapping("/{teamId}")
    public Response<MeetingResponseDTO.GetTeamDTO> getTeam(@PathVariable @Positive(message = "팀 ID는 양수여야 합니다.") Long teamId) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        MeetingResponseDTO.GetTeamDTO response = meetingQueryService.getTeam(userId, teamId);

        return Response.ok(response);
    }

    @Operation(summary = "우리 팀 조회")
    @GetMapping("/myTeam")
    public Response<MeetingResponseDTO.GetMyTeamDTO> getPreMyTeam(@RequestParam TeamType teamType) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        MeetingResponseDTO.GetMyTeamDTO response = meetingQueryService.getPreMyTeam(userId, teamType);

        return Response.ok(response);
    }

    @Operation(summary = "우리 팀 상세 조회")
    @GetMapping("/myTeam/detail")
    public Response<MeetingResponseDTO.GetTeamDTO> getMyTeam(@RequestParam TeamType teamType) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        MeetingResponseDTO.GetTeamDTO response = meetingQueryService.getMyTeam(userId, teamType);

        return Response.ok(response);
    }

    @Operation(summary = "우리 팀 하이 개수")
    @GetMapping("/myTeam/hi")
    public Response<MeetingResponseDTO.GetMyTeamHiDTO> getMyTeamHi(@RequestParam TeamType teamType) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        MeetingResponseDTO.GetMyTeamHiDTO response = meetingQueryService.getMyTeamHi(userId, teamType);

        return Response.ok(response);
    }

    @Operation(summary = "팀 만들기")
    @PostMapping("/myTeam")
    public Response<Void> creatTeam(@RequestParam TeamType teamType, @RequestBody MeetingRequestDTO.CreateTeamDTO request) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        meetingCommandService.createTeam(userId, teamType, request);

        return Response.ok();
    }

    @Operation(summary = "팀명 중복확인")
    @GetMapping("/teamName")
    public Response<MeetingResponseDTO.CheckNameDTO> checkName(@RequestParam @Size(min = 1, max = 7) String name) {

        MeetingResponseDTO.CheckNameDTO response = meetingQueryService.checkName(name);

        return Response.ok(response);
    }

    @Operation(summary = "하이 보내기")
    @PostMapping("/hi/send")
    public Response<String> sendHi(@RequestBody MeetingRequestDTO.hiDto hiDto){
        meetingQueryService.sendHi(hiDto);
        return Response.ok(hiDto.getToId() +"팀에게 하이가 보내졌습니다. ");
    }

    @Operation(summary = "하이 거절하기")
    @PatchMapping("/hi/refuse")
    public Response<String> refuseHi(@RequestBody MeetingRequestDTO.hiDto hiDto){
        meetingQueryService.refuseHi(hiDto);
        return Response.ok(hiDto.getFromId() +"팀이 보낸 하이가 거절되었습니다. ");
    }

    @Operation(summary = "받은 하이 목록")
    @GetMapping("/hiList/recevie")
    public Response<List<MeetingResponseDTO.hiListDto>> receiveHiList(@RequestParam Long teamId) {
        return Response.ok(meetingQueryService.checkHiList(teamId, "Receive"));
    }

    @Operation(summary = "보낸 하이 목록")
    @GetMapping("/hiList/send")
    public Response<List<MeetingResponseDTO.hiListDto>> sendHiList(@RequestParam Long teamId) {
        return Response.ok(meetingQueryService.checkHiList(teamId, "Send"));
    }



}