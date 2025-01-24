package com.gdg.z_meet.domain.order.controller;

import com.gdg.z_meet.domain.order.dto.PaymentConfirmRequest;
import com.gdg.z_meet.domain.order.dto.PaymentConfirmResponse;
import com.gdg.z_meet.domain.order.service.PaymentService;
import com.gdg.z_meet.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}")
    public Response<PaymentConfirmResponse> confirm(
            @RequestBody PaymentConfirmRequest request,
            @PathVariable String orderId) {


        return Response.ok();
    }



}
