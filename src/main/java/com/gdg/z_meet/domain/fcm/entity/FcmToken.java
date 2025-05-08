package com.gdg.z_meet.domain.fcm.entity;

import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;         // 하나의 유저에 연결되어 있는 다수의 디바이스 푸시 알림 고려

    @Column(unique = true)
    private String token;


    public FcmToken(final String token, final User user) {
        this.token = token;
        this.user = user;
    }

    public void updateToken(String newToken) {
        this.token = newToken;
    }

}