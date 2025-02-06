package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.TeamType;

public interface MeetingQueryService {

    MeetingResponseDTO.GetTeamGalleryDTO getTeamGallery(Long userId, TeamType teamType, Integer page);
    MeetingResponseDTO.GetTeamDTO getTeam(Long userId, Long teamId);
    MeetingResponseDTO.GetTeamDTO getMyTeam(Long userId, TeamType teamType);
}
