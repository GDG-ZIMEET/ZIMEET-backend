package com.gdg.z_meet.user.dto;

import com.gdg.z_meet.user.entity.profile.*;
import lombok.*;

public class UserReq {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class LoginReq{
        private String studentNumber;
        private String password;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class SignUpReq{
        private String name;
        private String studentNumber;
        private String password;
        private Grade grade;
        private int age;
        private Gender gender;
        private Major major;
        private String nickname;
        private Emoji emoji;
        private MBTI mbti;
        private Music music;
        private Style style;
        private IdealAge idealAge;
        private IdealType idealType;
    }
}
