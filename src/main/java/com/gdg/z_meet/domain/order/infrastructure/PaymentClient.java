package com.gdg.z_meet.domain.order.infrastructure;

import com.gdg.z_meet.domain.order.PaymentFeignConfig;
import com.gdg.z_meet.domain.order.dto.PaymentConfirmRequest;
import com.gdg.z_meet.domain.order.dto.PaymentConfirmResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

// FeignClient 를 통해 스프링이 Impl 클래스 자동 생성 및 토스 서버에 결제 승인 요청 보냄
@FeignClient(name = "paymentClient",
             url = "${payment.base-url}",
             configuration = PaymentFeignConfig.class)
public interface PaymentClient {

    @PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    PaymentConfirmResponse confirmPayment(@RequestBody PaymentConfirmRequest paymentConfirmRequest);


}