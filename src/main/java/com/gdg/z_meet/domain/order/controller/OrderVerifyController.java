package com.gdg.z_meet.domain.order.controller;

import com.gdg.z_meet.domain.order.dto.SaveAmountRequest;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.response.Code;
import com.gdg.z_meet.global.response.Response;
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
public class OrderVerifyController {

    @Operation(summary="주문 정보 임시 저장 API" ,
               description = "주문 Id 와 주문 금액을 세션에 임시 저장합니다. 이는, 클라이언트의 악의적 조작을 방지하기 위함입니다.")
    @PostMapping("/save-amount")
    public Response<String> tempSave(HttpSession session, @Valid @RequestBody SaveAmountRequest saveAmountRequest) {

        // 세션 유효성 검증
        if (session == null) {
            throw new BusinessException(Code.SESSION_EXPIRED);
        }

        session.setAttribute(saveAmountRequest.getOrderId(), saveAmountRequest.getAmount());

        // 저장 검증
        Integer storedAmount = (Integer) session.getAttribute(saveAmountRequest.getOrderId());
        if (storedAmount == null || !storedAmount.equals(saveAmountRequest.getAmount())) {
            throw new BusinessException(Code.SESSION_STORAGE_FAILED);
        }

        return Response.ok("Payment temp save successful");
    }

    @Operation(summary="주문 검증 API" ,
            description = "결제 전의 금액과 결제 후의 금액이 같은 지를 통해 주문을 검증합니다. 이상이 없다면, 세션에 저장된 정보는 삭제됩니다.")
    @PostMapping("/verify-amount")
    public Response<String> verifyAmount(HttpSession session, @Valid @RequestBody SaveAmountRequest saveAmountRequest) {

        // orderId에 해당하는 결제 금액
        Integer storedAmount = (Integer) session.getAttribute(saveAmountRequest.getOrderId());

        // 결제 전의 금액과 결제 후의 금액이 같은지 검증
        if(storedAmount == null || !storedAmount.equals(saveAmountRequest.getAmount())) {
            throw new BusinessException(Code.INVALID_PAYMENT_AMOUNT);
        }

        session.removeAttribute(saveAmountRequest.getOrderId());    // 검증에 사용했던 세션은 삭제

        return Response.ok("Payment is valid");
    }
}
