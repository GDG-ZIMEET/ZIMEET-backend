package com.gdg.z_meet.entity;

import com.gdg.z_meet.entity.common.BaseEntity;
import com.gdg.z_meet.entity.enums.Gender;
import com.gdg.z_meet.entity.enums.TeamType;
import jakarta.persistence.*;
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

    private TeamType teamType;

    private String name;

    private Gender gender;

    private Integer hi;
}
