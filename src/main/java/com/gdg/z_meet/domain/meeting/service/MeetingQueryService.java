package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;

public interface MeetingQueryService {

    MeetingResponseDTO.GetTeamDTO getTeam(Long userId, Long teamId);
}
