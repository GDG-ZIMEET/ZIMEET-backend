package com.gdg.z_meet.domain.fcm.service.custom;

import com.gdg.z_meet.domain.fcm.service.FcmMessageClient;
import com.gdg.z_meet.domain.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmProfileMessageService {

    private final FcmMessageClient fcmMessageClient;

    // 프로필 조회 API 호출 시 실행되어야
    @Transactional
    public void messagingProfileViewOneOneUsers(List<UserProfile> profiles) {
        Map<Integer, String> messageTitles = Map.of(
                10, "🥳 내 프로필을 10명이나 봤어요! 🎉 오늘도 인기 폭발 시작이에요!",
                50, "🔥 벌써 50명이 다녀갔어요! 대세는 역시 나, 지금 확인해보세요!",
                100, "💯 무려 100명이 당신을 봤어요! 관심 폭주 중이에요, 놓치지 마세요!",
                500, "🌟 500명 돌파! 이 정도면 거의 스타 등장이죠? 지금 확인해봐요!",
                1000, "🏆 1000명 초과 달성! ZI밋에서 당신의 인기가 뜨겁게 타오르고 있어요!"
        );
        String body = "인기있는 당신! 어떤 사람들이 ZI밋에 있는지 확인해볼까요?🔥";

        for (UserProfile profile : profiles) {
            int viewCount = profile.getViewCount();
            Long userId = profile.getUser().getId();

            if (messageTitles.containsKey(viewCount)) {
                String title = messageTitles.get(viewCount);
                try {
                    fcmMessageClient.sendFcmMessage(userId, title, body);
                } catch (Exception e) {
                    log.error("FCM 프로필 조회 수 알림 전송 실패 - userId: {}, message: {}", userId, e.getMessage(), e);
                }
            }
        }
    }


    @Transactional
    @Scheduled(fixedRate = 60000)      // 1분마다 실행
    public void messagingProfileViewTwoTwoUsers() {


        String title = "👀 아직 내 프로필이 활성화되지 않았어요.";
        String body = "연애 지수 폭발 중!🔥어떤 팀들이 기다리고 있는지 ZI밋에서 확인해보세요!";

    }
}
