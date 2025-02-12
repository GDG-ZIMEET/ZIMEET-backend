package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.dto.MeetingRequestDTO;
import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.TeamType;
import org.springframework.web.bind.annotation.RequestParam;

public interface MeetingQueryService {

    MeetingResponseDTO.GetTeamGalleryDTO getTeamGallery(Long userId, TeamType teamType, Integer page);
    MeetingResponseDTO.GetTeamDTO getTeam(Long userId, Long teamId);
    MeetingResponseDTO.GetMyTeamDTO getPreMyTeam(Long userId, TeamType teamType);
    MeetingResponseDTO.GetTeamDTO getMyTeam(Long userId, TeamType teamType);
    MeetingResponseDTO.GetMyTeamHiDTO getMyTeamHi(Long userId, TeamType teamType);
    MeetingResponseDTO.CheckNameDTO checkName(String name);
    void sendHi(MeetingRequestDTO.hiDto hiDto);
    void refuseHi(MeetingRequestDTO.hiDto hiDto);
}
