package com.gdg.z_meet.domain.fcm.service;

import com.gdg.z_meet.domain.fcm.entity.FcmToken;
import com.gdg.z_meet.domain.fcm.repository.FcmTokenRepository;
import com.gdg.z_meet.domain.user.dto.UserReq;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class FcmServiceImpl implements FcmService {

    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;

    /*
    String title = "채팅방 나가기 알림";
    String body = String.format("'%s' 채팅방에서 나갔습니다.", chatRoom.getName());
    fcmService.sendFcmMessage(user, title, body);
     */
    @Transactional
    public void sendFcmMessage(Long userId, String title, String body ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(Code.USER_NOT_FOUND));

        if (!user.isPushAgree()) {
            log.info("푸시 알림 비동의 상태: userId={}", userId);
            return;
        }

        List<FcmToken> tokens = fcmTokenRepository.findAllByUser(user);
        if (tokens.isEmpty()) {
            log.warn("FCM 토큰 없음: userId={}", userId);
            return;
        }

        // 유저 당 디바이스는 많아야 3개로 예상되므로 , for 문으로 순차 처리
        for (FcmToken deviceToken : tokens) {
            String token = deviceToken.getToken();
            log.info("FCM 전송 대상 토큰: {}", token);

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            try {
                String response = FirebaseMessaging.getInstance().send(message);   // FCM 서버에 메시지 전송
                log.info("FCM 전송 성공: {}", response);
            } catch (FirebaseMessagingException e) {
                log.warn("FCM 전송 실패: {}", e.getMessage(), e);

                Set<String> deletableErrorCodes = Set.of(
                        "registration-token-not-registered",
                        "invalid-argument",
                        "messaging/invalid-registration-token",
                        "unregistered"
                );

                // 해당 에러 코드일 경우 토큰 삭제
                if (deletableErrorCodes.contains(e.getErrorCode())) {
                    fcmTokenRepository.delete(deviceToken);
                    log.warn("무효한 FCM 토큰 삭제: token={}, userId={}", token, user.getId());
                }

                // 예외 throw 하지 않으므로, 하나 전송 실패해도 계속해서 나머지 토큰에는 알림 발송
            }
        }
    }


    @Override
    @Transactional
    public boolean agreePush(Long userId, UserReq.pushAgreeReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(Code.USER_NOT_FOUND));

        user.setPushAgree(req.isPushAgree());
        return user.isPushAgree();
    }

    @Override
    @Transactional
    public void syncFcmToken(Long userId, UserReq.saveFcmTokenReq req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(Code.USER_NOT_FOUND));

        // 기기 변경 시, 토큰이 변경되므로 기존 토큰을 덮어쓰는 방식 필요 => 기존에 있었다면 갱신
        boolean exists = fcmTokenRepository.existsByUserAndToken(user, req.getFcmToken());

        if (!exists) {
            fcmTokenRepository.save(FcmToken.builder()
                    .user(user)
                    .token(req.getFcmToken())
                    .build());
        }
    }


    @Override
    public void testFcmService(Long userId, String fcmToken) {

        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(Code.USER_NOT_FOUND));

        log.info("받은 FCM 토큰 값 : " + fcmToken);

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                        Notification.builder()
                                .setTitle("ZI-MEET FCM 알림 테스트입니다.")
                                .setBody("테스트 성공했나요?")
                                .build())
                .build();
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 응답: {}", response);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 전송 실패: {}", e.getMessage(), e);
            throw new BusinessException(Code.FCM_SEND_MESSAGE_ERROR);
        }
    }
}
