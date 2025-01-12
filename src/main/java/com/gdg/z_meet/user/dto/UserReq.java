package com.gdg.z_meet.user.dto;

import com.gdg.z_meet.user.entity.profile.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

public class UserReq {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class LoginReq{
        @NotNull(message = "학번을 입력해주세요.")
        private String studentNumber;
        @NotNull(message = "비밀번호를 입력해주세요.")
        private String password;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class SignUpReq{
        @NotNull(message = "이름 입력은 필수입니다.")
        private String name;
        @NotNull(message = "학번 입력은 필수입니다.")
        private String studentNumber;
        @NotNull(message = "비밀번호 입력은 필수입니다.")
        private String password;

        //유저 상세정보
        @NotNull(message = "닉네임 입력은 필수입니다.")
        @Size(min = 2, max = 6, message = "닉네임은 2자 이상 6자 이하로 입력해주세요.")
        private String nickname;
        private Grade grade;
        @Size(min = 19, max = 29, message = "19세에서 29세까지 이용 가능합니다.")
        private int age;
        private Gender gender;
        private Major major;
        private Emoji emoji;
        private MBTI mbti;
        private Music music;
        private Style style;
        private IdealAge idealAge;
        private IdealType idealType;
    }
}
