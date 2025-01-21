package com.gdg.z_meet.domain.user.dto;

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
}
