package com.gdg.z_meet.domain.meeting.controller;

import com.gdg.z_meet.domain.meeting.dto.MeetingRequestDTO;
import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.enums.TeamType;
import com.gdg.z_meet.domain.meeting.service.HiQueryService;
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
    private final HiQueryService hiQueryService;

    @Operation(summary = "팀 갤러리 조회", description = "12팀씩 페이징 됩니다.")
    @GetMapping
    public Response<MeetingResponseDTO.GetTeamGalleryDTO> getTeamGallery(@RequestParam(name = "teamType") TeamType teamType, @RequestParam(name = "page") Integer page) {

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
    public Response<MeetingResponseDTO.GetPreMyTeamDTO> getPreMyTeam(@RequestParam(name = "teamType") TeamType teamType) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        MeetingResponseDTO.GetPreMyTeamDTO response = meetingQueryService.getPreMyTeam(userId, teamType);

        return Response.ok(response);
    }

    @Operation(summary = "우리 팀 상세 조회")
    @GetMapping("/myTeam/detail")
    public Response<MeetingResponseDTO.GetMyTeamDTO> getMyTeam(@RequestParam(name = "teamType") TeamType teamType) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        MeetingResponseDTO.GetMyTeamDTO response = meetingQueryService.getMyTeam(userId, teamType);

        return Response.ok(response);
    }

    @Operation(summary = "우리 팀 하이 개수")
    @GetMapping("/myTeam/hi")
    public Response<MeetingResponseDTO.GetMyTeamHiDTO> getMyTeamHi(@RequestParam(name = "teamType") TeamType teamType) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        MeetingResponseDTO.GetMyTeamHiDTO response = meetingQueryService.getMyTeamHi(userId, teamType);

        return Response.ok(response);
    }

    @Operation(summary = "팀 만들기")
    @PostMapping("/myTeam")
    public Response<Void> creatTeam(@RequestParam(name = "teamType") TeamType teamType, @RequestBody MeetingRequestDTO.CreateTeamDTO request) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        meetingCommandService.createTeam(userId, teamType, request);

        return Response.ok();
    }

    @Operation(summary = "팀명 중복확인")
    @GetMapping("/teamName")
    public Response<MeetingResponseDTO.CheckNameDTO> checkName(@RequestParam(name = "name") @Size(min = 1, max = 8) String name) {

        MeetingResponseDTO.CheckNameDTO response = meetingQueryService.checkName(name);

        return Response.ok(response);
    }


    @Operation(summary = "하이 보내기")
    @PostMapping("/hi/send")
    public Response<String> sendHi(@RequestBody MeetingRequestDTO.hiDto hiDto){
        hiQueryService.sendHi(hiDto);
        return Response.ok(hiDto.getToId() +"팀에게 하이가 보내졌습니다. ");
    }

    @Operation(summary = "하이 거절하기")
    @PatchMapping("/hi/refuse")
    public Response<String> refuseHi(@RequestBody MeetingRequestDTO.hiDto hiDto){
        hiQueryService.refuseHi(hiDto);
        return Response.ok(hiDto.getFromId() +"팀이 보낸 하이가 거절되었습니다. ");
    }

    @Operation(summary = "받은 하이 목록")
    @GetMapping("/hiList/receive")
    public Response<List<MeetingResponseDTO.hiListDto>> receiveHiList() {
        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        return Response.ok(hiQueryService.checkHiList(userId, "Receive"));
    }

    @Operation(summary = "보낸 하이 목록")
    @GetMapping("/hiList/send")
    public Response<List<MeetingResponseDTO.hiListDto>> sendHiList() {
        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        return Response.ok(hiQueryService.checkHiList(userId, "Send"));
    }


    @Operation(summary = "팀원 검색하기")
    @GetMapping("/search")
    public Response<MeetingResponseDTO.GetSearchListDTO> getSearch(@RequestParam(name = "teamType") TeamType teamType,
                                                                   @RequestParam(name = "nickname", required = false) @Size(min = 1, max = 8) String nickname,
                                                                   @RequestParam(name = "phoneNumber", required = false) @Size(min = 1, max = 11) String phoneNumber) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        MeetingResponseDTO.GetSearchListDTO response = meetingQueryService.getSearch(userId, teamType, nickname, phoneNumber);

        return Response.ok(response);
    }

    @Operation(summary = "팀 삭제하기")
    @DeleteMapping("/myTeam")
    public Response<Void> delTeam(@RequestParam(name = "teamType") TeamType teamType) {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        meetingCommandService.delTeam(userId, teamType);

        return Response.ok();
    }

    @Operation(summary = "팀 삭제 기회")
    @GetMapping("/myTeam/delete")
    public Response<MeetingResponseDTO.GetMyDeleteDTO> getMyDelete() {

        Long userId = AuthenticatedUserUtils.getAuthenticatedUserId();
        MeetingResponseDTO.GetMyDeleteDTO response = meetingQueryService.getMyDelete(userId);

        return Response.ok(response);
    }
}