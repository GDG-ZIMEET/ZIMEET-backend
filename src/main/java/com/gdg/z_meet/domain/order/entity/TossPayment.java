package com.gdg.z_meet.domain.order.entity;

import com.gdg.z_meet.domain.order.dto.response.ConfirmPaymentRes;
import com.gdg.z_meet.global.Util.UUIDUtil;
import com.gdg.z_meet.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Base64;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TossPayment extends BaseEntity {

    @Id
    @Column(name = "toss_payments_id", unique = true)
    private String paymentId;           // uuid

    @Column(nullable = false, unique = true)
    private String tossPaymentKey;

    // 토스 내부에서 관리하는 별도의 orderId가 존재
    @Column(nullable = false)
    private String tossOrderId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

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

    public static TossPayment create(
            String tossPaymentKey,
            String tossOrderId,
            Order order,
            long totalAmount,
            TossPaymentMethod tossPaymentMethod,
            TossPaymentStatus tossPaymentStatus,
            LocalDateTime requestedAt,
            LocalDateTime approvedAt
    ) {
        return TossPayment.builder()
                .paymentId(Base64.getEncoder().encodeToString(UUIDUtil.createUUID()))
                .tossPaymentKey(tossPaymentKey)
                .order(order)
                .tossOrderId(tossOrderId)
                .totalAmount(totalAmount)
                .tossPaymentMethod(tossPaymentMethod)
                .tossPaymentStatus(tossPaymentStatus)
                .requestedAt(requestedAt)
                .approvedAt(approvedAt)
                .build();
    }

    public ConfirmPaymentRes toResponse() {
        return ConfirmPaymentRes.create(
                UUIDUtil.bytesToHex(order.getOrderId()),      // 다시 UUID 형식으로 변환(backendOrderId)
                                    tossPaymentMethod,
                                    tossPaymentStatus,
                                    totalAmount);
    }

    public void changePaymentStatus(TossPaymentStatus newStatus) {
        this.tossPaymentStatus = newStatus;
    }

}
