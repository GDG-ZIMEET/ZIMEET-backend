package com.gdg.z_meet.domain.user.entity;

import com.gdg.z_meet.domain.user.entity.enums.*;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name="user_profile")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_profile_id", unique = true, nullable = false)
    private Long id;

//    @OneToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Emoji emoji;

    @Column(nullable = false)
    private Music music;

    @Column(nullable = false)
    private MBTI mbti;

    @Column(nullable = false)
    private Style style;

    @Column(nullable = false)
    private IdealType ideal_type;

    @Column(nullable = false)
    private IdealAge ideal_age;

    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private Grade grade;

    @Column(nullable = false)
    private Major major;

    @Column(nullable = false)
    private int age;

    private int delete;
}
