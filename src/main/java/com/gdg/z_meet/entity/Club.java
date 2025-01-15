package com.gdg.z_meet.entity;

import com.gdg.z_meet.entity.common.BaseEntity;
import com.gdg.z_meet.entity.enums.Category;
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

    private String name;

    private String rep;

    private Category category;

    private String account;

    private String time;

    private String info;
}
