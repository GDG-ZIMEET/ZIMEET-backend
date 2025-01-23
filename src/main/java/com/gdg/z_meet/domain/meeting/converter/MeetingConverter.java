package com.gdg.z_meet.domain.meeting.converter;

import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.Team;

public class MeetingConverter {

    public static MeetingResponseDTO.GetTeamDTO toGetTeamDTO(Team team){

        return MeetingResponseDTO.GetTeamDTO.builder()
                .teamId(team.getId())
                .name(team.getName())
                .build();
    }
}
