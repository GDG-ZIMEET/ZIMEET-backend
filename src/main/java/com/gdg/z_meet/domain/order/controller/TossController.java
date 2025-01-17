package com.gdg.z_meet.domain.order.controller;

import com.gdg.z_meet.domain.order.dto.SaveAmountRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class TossController {

    @Operation(summary="주문 정보 임시 저장 API" ,
               description = "주문 Id 와 주문 금액을 세션에 임시 저장합니다. 이는, 클라이언트의 악의적 조작을 방지하기 위함입니다.")
    @PostMapping("/save-amount")
    public String tempSave(HttpSession session, @Valid @RequestBody SaveAmountRequest saveAmountRequest) {

        session.setAttribute(saveAmountRequest.getOrderId(), saveAmountRequest.getAmount());

        return "Payment temp save successful";
    }
}
