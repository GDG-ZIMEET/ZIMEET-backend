package com.gdg.z_meet.domain.meeting.dto;

import com.gdg.z_meet.domain.user.entity.enums.Major;
import com.gdg.z_meet.domain.user.entity.enums.Music;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
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

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetMyTeamHiDTO {
        Integer hi;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckNameDTO {
        Integer check;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class hiListDto {
        Long myTeamId;
        Long teamId;
        String teamName;
        List<UserProfileDto> userProfileDtos;
        Double age;
        String dateTime;

        @Getter
        @AllArgsConstructor
        @Builder
        public static class UserProfileDto {
            private Major major;
            private String emoji;
            private Music music;
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetSearchDTO {
        Long userId;
        String nickname;
        String major;
        String grade;
        String phoneNumber;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetSearchListDTO {
        List<GetSearchDTO> searchList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetMyDeleteDTO {
        Integer leftDelete;

    }
}
