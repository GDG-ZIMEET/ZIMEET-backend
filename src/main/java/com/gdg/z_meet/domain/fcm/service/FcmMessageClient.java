package com.gdg.z_meet.domain.fcm.service;

import com.gdg.z_meet.domain.fcm.entity.FcmToken;
import com.gdg.z_meet.domain.fcm.repository.FcmTokenRepository;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmMessageClient {

    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public void sendFcmMessage(Long userId, String title, String body ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(Code.USER_NOT_FOUND));

        if (!user.isPushAgree()) {
            log.info("푸시 알림 비동의 상태: userId={}", userId);
            return;
        }

        FcmToken userToken = fcmTokenRepository.findByUser(user).orElse(null);

        if (userToken == null || userToken.getToken() == null || userToken.getToken().isBlank()) {
            log.warn("FCM 토큰 없음 또는 유효하지 않음: userId={}", userId);
            return;
        }

        String token = userToken.getToken();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);   // FCM 서버에 메시지 전송
            log.info("FCM 전송 성공: userId={},  response={}", userId, response);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 전송 실패: userId={}, error={}", userId, e.getMessage(), e);

            Set<String> deletableErrorCodes = Set.of(
                    "UNREGISTERED",
                    "INVALID_ARGUMENT", "INVALID_ARGUMENTS",
                    "registration-token-not-registered",
                    "invalid-argument",
                    "messaging/invalid-registration-token",
                    "unregistered"
            );

            // 해당 에러 코드일 경우 토큰 삭제
            String errorCode = String.valueOf(e.getErrorCode());
            if (errorCode != null && deletableErrorCodes.contains(errorCode.toUpperCase())) {
                fcmTokenRepository.delete(userToken);
                log.warn("무효한 FCM 토큰 삭제: token={}, userId={}", token, user.getId());
            }
        }
    }
}
