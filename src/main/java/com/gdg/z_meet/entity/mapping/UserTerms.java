package com.gdg.z_meet.entity.mapping;

import com.gdg.z_meet.entity.Terms;
import com.gdg.z_meet.entity.User;
import com.gdg.z_meet.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserTerms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_terms_id", unique = true)
    private Long id;

    @Column(nullable = false)
    private Boolean agree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id")
    private Terms terms;
}
