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
