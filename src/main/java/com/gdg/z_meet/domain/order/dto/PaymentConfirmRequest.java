package com.gdg.z_meet.domain.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentConfirmRequest {

    private String orderId;
    private int amount;
    private String paymentKey;

    public PaymentConfirmRequest(String orderId, int amount, String paymentKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
    }
}
