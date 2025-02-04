package com.gdg.z_meet.domain.meeting.converter;

import com.gdg.z_meet.domain.meeting.dto.MeetingResponseDTO;
import com.gdg.z_meet.domain.meeting.entity.Team;
import com.gdg.z_meet.domain.user.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gdg.z_meet.domain.meeting.entity.Verification.COMPLETE;

public class MeetingConverter {

    public static MeetingResponseDTO.GetTeamGalleryDTO toGetTeamGalleryDTO(Page<Team> teamList,
                                                                           Map<Long, List<String>> emojiList,
                                                                           Map<Long, List<String>> majorList,
                                                                           Map<Long, Double> age,
                                                                           Map<Long, List<String>> musicList){

        List<MeetingResponseDTO.GetPreTeamDTO> teamDTOS = teamList.stream()
                .map(team -> MeetingResponseDTO.GetPreTeamDTO.builder()
                        .teamId(team.getId())
                        .emoji(emojiList.get(team.getId()))
                        .name(team.getName())
                        .verification(team.getVerification() == COMPLETE ? 1 : 0)
                        .major(majorList.get(team.getId()))
                        .age(Math.round(age.get(team.getId()) * 10.0) / 10.0)
                        .music(musicList.get(team.getId()))
                        .build())
                .collect(Collectors.toList());

        return MeetingResponseDTO.GetTeamGalleryDTO.builder()
                .teamList(teamDTOS)
                .build();
    }

    public static MeetingResponseDTO.GetTeamDTO toGetTeamDTO(Team team, List<User> users){

        List<MeetingResponseDTO.GetTeamUserDTO> teamUserDTOS = users.stream()
                .map(user -> MeetingResponseDTO.GetTeamUserDTO.builder()
                        .userId(user.getId())
                        .emoji(String.valueOf(user.getUserProfile().getEmoji()))
                        .nickname(user.getUserProfile().getNickname())
                        .age(user.getUserProfile().getAge())
                        .studentNumber(user.getStudentNumber().substring(2, 4))
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
