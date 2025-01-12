package com.gdg.z_meet.user.entity;

import com.gdg.z_meet.user.entity.profile.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_profile_id", unique = true, nullable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)

    @Column(unique = true, nullable = false)
    private String nickname;

    private Emoji emoji;

    private Music music;

    private MBTI mbti;

    private Style style;

    private IdealType ideal_type;

    private Gender gender;

    private Grade grade;

    private Major major;

    private int age;

    private int delete;
}
