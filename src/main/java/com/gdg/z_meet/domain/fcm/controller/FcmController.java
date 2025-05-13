package com.gdg.z_meet.domain.fcm.controller;

import com.gdg.z_meet.domain.fcm.dto.FcmTestReq;
import com.gdg.z_meet.domain.fcm.service.FcmService;
import com.gdg.z_meet.domain.user.dto.UserReq;
import com.gdg.z_meet.global.response.Response;
import com.gdg.z_meet.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
public class FcmController {

    private final FcmService fcmService;

    @Operation(summary = "푸시 알림 동의 API", description = "푸시 알림 동의 API 입니다.")
    @Parameters({
            @Parameter(name = "user", hidden = true)
    })
    @PostMapping("/push-agree")
    Response<String> pushAgree(@AuthUser Long userId, @RequestBody UserReq.pushAgreeReq req) {
        boolean pushAgree = fcmService.agreePush(userId, req);
        String responseMessage = pushAgree ? "푸시 알림 허용" : "푸시 알림 거부";
        return Response.ok(responseMessage);
    }


    @Operation(summary = "FCM 토큰 저장 API", description = "FCM 토큰 저장 API 입니다.")
    @Parameters({
            @Parameter(name = "user", hidden = true)
    })
    @PostMapping("/token")
    public Response<Long> syncFcmToken(@AuthUser Long userId, @RequestBody UserReq.saveFcmTokenReq req) {
        fcmService.syncFcmToken(userId, req);
        return Response.ok(userId);
    }


    @Operation(summary = "FCM 테스트 API", description = "테스트용 API 입니다.")
    @PostMapping("/test")
    public Response<Void> testFCM(@AuthUser Long userId, @RequestBody FcmTestReq req) {
        fcmService.testFcmService(userId, req.getToken());
        return Response.ok();
    }

    // 프론트에서 메시지 구성 시, 사용 예정
//    @Operation(summary = "FCM 메시지 전송 API", description = "FCM ")
//    @PostMapping("/send-alarm")
//    public Response<Void> pushMessage(@AuthUser User user) throws IOException {
//        fcmService.sendFcmMessage(user);
//        return Response.ok();
//    }
}
