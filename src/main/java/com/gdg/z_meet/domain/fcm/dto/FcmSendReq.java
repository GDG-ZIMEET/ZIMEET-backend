package com.gdg.z_meet.domain.fcm.dto;

import lombok.*;

import java.util.Map;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmSendReq {

    private String fcmToken;

    public FcmSendReq(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
