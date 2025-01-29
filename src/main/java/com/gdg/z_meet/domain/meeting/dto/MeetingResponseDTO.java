package com.gdg.z_meet.domain.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class MeetingResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetTeamUserDTO {
        Long userId;
        String emoji;
        String nickname;
        Integer age;
        String studentNumber;
        String major;
        String music;

        // 지밋 플러스
        String mbti;
        String style;
        String idealType;
        String idealAge;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetTeamDTO {
        Long teamId;
        String name;
        List<GetTeamUserDTO> userList;
    }
}
