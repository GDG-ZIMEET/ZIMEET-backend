package com.gdg.z_meet.entity;

import com.gdg.z_meet.entity.common.BaseEntity;
import com.gdg.z_meet.entity.enums.Category;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
    private List<Item> itemList = new ArrayList<>();
}
