package com.gdg.z_meet.entity;

import com.gdg.z_meet.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", unique = true)
    private Long id;

    @Column(nullable = false)
    private String content;

    private Boolean isRead;
}
