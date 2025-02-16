package com.gdg.z_meet.domain.meeting.service;

import com.gdg.z_meet.domain.meeting.dto.MeetingRequestDTO;
import com.gdg.z_meet.domain.meeting.entity.TeamType;

public interface MeetingCommandService {

    void createTeam(Long userId, TeamType teamType, MeetingRequestDTO.CreateTeamDTO request);
    void delTeam(Long userId, TeamType teamType);
}
