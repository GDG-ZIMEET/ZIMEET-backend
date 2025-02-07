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
    public static class GetPreTeamDTO {
        Long teamId;
        List<String> emoji;
        String name;
        Integer verification;
        List<String> major;
        Double age;
        List<String> music;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetTeamGalleryDTO {
        List<GetPreTeamDTO> teamList;
    }

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
        Integer verification;
        String gender;
        List<GetTeamUserDTO> userList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetMyTeamDTO {
        Long teamId;
        List<String> emoji;
        String name;
        Integer verification;
    }
}
