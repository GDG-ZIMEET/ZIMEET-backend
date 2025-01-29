package com.gdg.z_meet.domain.meeting.converter;

import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.meeting.entity.UserTeam;
import com.gdg.z_meet.domain.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class MeetingConverter {

    public static MeetingResponseDTO.GetTeamDTO toGetTeamDTO(Team team, List<User> users){

        List<MeetingResponseDTO.GetTeamUserDTO> teamUserDTOS = users.stream()
                .map(user -> MeetingResponseDTO.GetTeamUserDTO.builder()
                        .userId(user.getId())
                        .emoji(String.valueOf(user.getUserProfile().getEmoji()))
                        .nickname(user.getUserProfile().getNickname())
                        .age(user.getUserProfile().getAge())
                        .studentNumber(user.getStudentNumber())
                        .major(String.valueOf(user.getUserProfile().getMajor()))
                        .music(String.valueOf(user.getUserProfile().getMusic()))
                        .mbti(String.valueOf(user.getUserProfile().getMbti()))
                        .style(String.valueOf(user.getUserProfile().getStyle()))
                        .idealType(String.valueOf(user.getUserProfile().getIdealType()))
                        .idealAge(String.valueOf(user.getUserProfile().getIdealAge()))
                        .build())
                .collect(Collectors.toList());

        return MeetingResponseDTO.GetTeamDTO.builder()
                .teamId(team.getId())
                .name(team.getName())
                .userList(teamUserDTOS)
                .build();
    }
}
