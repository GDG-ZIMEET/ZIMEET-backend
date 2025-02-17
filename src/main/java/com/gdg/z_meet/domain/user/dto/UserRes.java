package com.gdg.z_meet.domain.user.dto;

import com.gdg.z_meet.domain.user.entity.enums.*;
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
        private Major major;
        private String emoji;
        private MBTI mbti;
        private Music music;
        private Style style;
        private IdealAge idealAge;
        private IdealType idealType;
    }

    @Getter
    @Builder
    public static class UserProfileRes {
        private String nickname;
        private String studentNumber;
        private Gender gender;
        private String emoji;
        private int age;
        private String major;
        private MBTI mbti;
        private Style style;
        private IdealType idealType;
        private IdealAge idealAge;
        private Music music;
    }

    @Getter
    @Builder
    public static class NicknameUpdateRes {
        private String nickname;
    }

    @Getter
    @Builder
    public static class EmojiUpdateRes {
        private String emoji;
    }
}
