package com.gdg.z_meet.domain.order.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TossPaymentMethod {
    CARD("카드"),
    VIRTUAL_ACCOUNT("가상계좌"),
    SIMPLE_PAYMENT("간편결제"),
    MOBILE("휴대폰"),
    BANK_TRANSFER("계좌이체");

    private final String description;
}
