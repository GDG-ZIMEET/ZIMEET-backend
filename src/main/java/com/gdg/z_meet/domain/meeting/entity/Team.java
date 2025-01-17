package com.gdg.z_meet.domain.meeting.entity;

import com.gdg.z_meet.global.common.BaseEntity;
import com.gdg.z_meet.domain.user.entity.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id", unique = true)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamType teamType;

    @Column(nullable = false)
    @Size(min = 1, max = 7)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    @Builder.Default
    private Integer hi = 2;
}
