package com.gdg.z_meet.domain.order.dto.request;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConfirmPaymentReq {
    private final String backendOrderId;       // 서버 내에서 주문 검증 로직에 사용되는 주문 ID
    private final String orderId;              // Toss 에서 결제를 위해 생성하는 주문 ID
    private final String amount;
    private final String paymentKey;
}