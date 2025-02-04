package com.gdg.z_meet.domain.order.dto.request;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CancelPaymentReq {

    private final String paymentKey;
    private final String cancelReason;

}
