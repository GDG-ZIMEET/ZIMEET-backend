package com.gdg.z_meet.domain.booth.entity;

import com.gdg.z_meet.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Club extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id", unique = true)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Place place;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String rep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    private String account;

    @Column(nullable = false)
    private String time;

    @Column(nullable = false)
    private String info;
}
