package com.gdg.z_meet.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SaveAmountRequest {

    @NotNull
    private String orderId;

    @NotNull
    private int amount;

//    {
//        "orderId": "MC42Nzg3MTc2OTQ5ODYz",
//        "amount": 1000
//    }
}
