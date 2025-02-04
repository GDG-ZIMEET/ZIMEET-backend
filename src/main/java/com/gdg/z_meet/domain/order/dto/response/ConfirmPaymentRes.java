package com.gdg.z_meet.domain.order.dto.response;

import com.gdg.z_meet.domain.order.entity.TossPaymentMethod;
import com.gdg.z_meet.domain.order.entity.TossPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ConfirmPaymentRes {

    private final String backendOrderId;
    private final TossPaymentMethod tossPaymentMethod;
    private final TossPaymentStatus tossPaymentStatus;
    private final long totalAmount;


    public static ConfirmPaymentRes create(String backendOrderId, TossPaymentMethod tossPaymentMethod, TossPaymentStatus tossPaymentStatus, long totalAmount) {
        return ConfirmPaymentRes.builder()
                .backendOrderId(backendOrderId)
                .tossPaymentMethod(tossPaymentMethod)
                .tossPaymentStatus(tossPaymentStatus)
                .totalAmount(totalAmount)
                .build();
    }
}