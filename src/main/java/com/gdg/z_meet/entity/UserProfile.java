package com.gdg.z_meet.entity;

import com.gdg.z_meet.entity.common.BaseEntity;
import com.gdg.z_meet.entity.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_profile_id", unique = true)
    private Long id;

    @Column(unique = true, nullable = false)
    @Size(min = 1, max = 7)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Emoji emoji;

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
    private IdealType ideal_type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdealAge ideal_age;

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
    @Size(min = 20, max = 28)
    private int age;

    @Column(nullable = false)
    @Builder.Default
    private int delete = 0;
}
