package com.gdg.z_meet.domain.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refreshTokenId;

    private String refreshToken;

    private String keyId;

    @Builder
    public RefreshToken(String refreshToken, String keyId) {
        this.refreshToken = refreshToken;
        this.keyId = keyId;
    }

    public void update(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
