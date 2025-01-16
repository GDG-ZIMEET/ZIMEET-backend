package com.gdg.z_meet.entity;

import com.gdg.z_meet.entity.common.BaseEntity;
import com.gdg.z_meet.entity.mapping.UserTeam;
import com.gdg.z_meet.entity.mapping.UserTerms;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", unique = true)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String studentNumber;

    @Column(nullable = false)
    private String password;
}
