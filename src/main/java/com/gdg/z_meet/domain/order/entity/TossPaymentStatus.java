package com.gdg.z_meet.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TossPaymentStatus {
    ABORTED("중단됨"),
    CANCELED("취소됨"),
    DONE("완료됨"),
    EXPIRED("만료됨"),
    IN_PROGRESS("진행 중"),
    PARTIAL_CANCELED("부분 취소됨"),
    READY("준비됨"),
    WAITING_FOR_DEPOSIT("입금 대기 중");

    private final String koreanName;

}
