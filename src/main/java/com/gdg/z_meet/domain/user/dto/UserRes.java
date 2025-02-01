package com.gdg.z_meet.domain.user.dto;

import com.gdg.z_meet.domain.user.entity.enums.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

public class UserRes {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class SignUpRes {
        private String message;
    }

    @Getter
    @Builder
    public static class ProfileRes {
        private String name;
        private String studentNumber;

        //유저 상세정보
        private String nickname;
        private Grade grade;
        private int age;
//        private Gender gender;
        private Major major;
        private Emoji emoji;
        private MBTI mbti;
//        private Music music;
        private Style style;
        private IdealAge idealAge;
        private IdealType idealType;
        }
}
