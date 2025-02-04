package com.gdg.z_meet.domain.order.entity;


import lombok.Getter;

@Getter
public enum TossPaymentMethod {
    EASY_PAYMENT("간편결제"),
    CARD("카드");


    private final String displayName;

    TossPaymentMethod(String displayName) {
        this.displayName = displayName;
    }

}
