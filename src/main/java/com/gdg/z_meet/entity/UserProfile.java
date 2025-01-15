package com.gdg.z_meet.entity;

import com.gdg.z_meet.entity.common.BaseEntity;
import com.gdg.z_meet.entity.enums.*;
import jakarta.persistence.*;
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
    private String nickname;

    private Emoji emoji;

    private Music music;

    private MBTI mbti;

    private Style style;

    private IdealType ideal_type;

    private IdealAge ideal_age;

    private Gender gender;

    private Grade grade;

    private Major major;

    private int age;

    private int delete;
}
