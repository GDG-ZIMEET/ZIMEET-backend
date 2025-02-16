package com.gdg.z_meet.domain.user.entity;

import com.gdg.z_meet.domain.user.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;

import static com.gdg.z_meet.domain.user.entity.enums.Level.LIGHT;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_profile_id", unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String emoji;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Music music;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MBTI mbti;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Style style;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdealType idealType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdealAge idealAge;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Major major;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    @Builder.Default
    private int deleteTeam = 0;

    @Column(nullable = false)
    @Builder.Default
    private Level level = LIGHT;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
