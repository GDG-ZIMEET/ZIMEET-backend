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
    private int leftDelete = 2;
    
    @Column(nullable = false)
    @Builder.Default
    private Level level = Level.LIGHT;

    @Column(nullable = false)
    @Builder.Default
    private int ticket = 2;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isDeleted;

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void increaseTicket(int amount) {
        this.ticket += amount;
    }

    public void decreaseTicket(int amount) {
        this.ticket -= amount;
    }

    public void upgradeToPlus() {
        this.level = Level.PLUS;
    }

    public void addDelete() { this.leftDelete++; }

    public void upLevel() { this.level = Level.PLUS; }

    public void setNeulTicket() { this.ticket = 99; }
}
