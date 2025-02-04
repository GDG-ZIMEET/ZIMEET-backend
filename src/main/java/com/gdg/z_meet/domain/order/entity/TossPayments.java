package com.gdg.z_meet.domain.order.entity;

import com.gdg.z_meet.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TossPayments extends BaseEntity {

    @Id
    @Column(name = "toss_payments_id", unique = true)
    private String paymentId;           // uuid

    @Column(nullable = false, unique = true)
    private String tossPaymentKey;

    // 토스 내부에서 관리하는 별도의 orderId가 존재
    @Column(nullable = false)
    private String tossOrderId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id", nullable = false)
    private Orders orders;

    private long totalAmount;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private TossPaymentMethod tossPaymentMethod;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private TossPaymentStatus tossPaymentStatus;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

}
