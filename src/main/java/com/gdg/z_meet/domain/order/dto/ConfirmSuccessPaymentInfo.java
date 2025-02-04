package com.gdg.z_meet.domain.order.dto;


import com.gdg.z_meet.domain.order.entity.Order;
import com.gdg.z_meet.domain.order.entity.TossPayment;
import com.gdg.z_meet.domain.order.entity.TossPaymentMethod;
import com.gdg.z_meet.domain.order.entity.TossPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class ConfirmSuccessPaymentInfo {

    private final String backendOrderId;
    private final String tossOrderId;
    private final long totalAmount;
    private final String tossPaymentKey;
    private final TossPaymentMethod tossPaymentMethod;
    private final TossPaymentStatus tossPaymentStatus;
    private final LocalDateTime requestedAt;
    private final LocalDateTime approvedAt;


    public static ConfirmSuccessPaymentInfo create(
            String backendOrderId,
            String tossOrderId,
            String tossPaymentKey,
            TossPaymentMethod tossPaymentMethod,
            TossPaymentStatus tossPaymentStatus,
            long totalAmount,
            LocalDateTime requestedAt,
            LocalDateTime approvedAt) {

        return ConfirmSuccessPaymentInfo.builder()
                .backendOrderId(backendOrderId)
                .tossOrderId(tossOrderId)
                .tossPaymentKey(tossPaymentKey)
                .tossPaymentMethod(tossPaymentMethod)
                .tossPaymentStatus(tossPaymentStatus)
                .totalAmount(totalAmount)
                .requestedAt(requestedAt)
                .approvedAt(approvedAt)
                .build();
    }

    // TossPayment 엔티티로 변환하는 메서드
    public TossPayment toTossPayment(Order order) {
        return TossPayment.create(
                tossPaymentKey,
                tossOrderId,
                order,
                totalAmount,
                tossPaymentMethod,
                tossPaymentStatus,
                requestedAt,
                approvedAt
        );
    }
}
