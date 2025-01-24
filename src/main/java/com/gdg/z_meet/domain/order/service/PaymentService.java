package com.gdg.z_meet.domain.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.z_meet.domain.order.PaymentProperties;
import com.gdg.z_meet.domain.order.dto.PaymentConfirmRequest;
import com.gdg.z_meet.domain.order.infrastructure.PaymentAuthInterceptor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class PaymentService {
    private final ObjectMapper objectMapper;
    private final PaymentAuthInterceptor paymentAuthInterceptor;
    private final PaymentProperties paymentProperties;

    public PaymentService(ObjectMapper objectMapper, PaymentAuthInterceptor paymentAuthInterceptor, PaymentProperties paymentProperties) {
        this.objectMapper = objectMapper;
        this.paymentAuthInterceptor = paymentAuthInterceptor;
        this.paymentProperties = paymentProperties;
    }




    public HttpResponse requestConfirm(PaymentConfirmRequest confirmPaymentRequest) throws IOException, InterruptedException {
        String tossOrderId = confirmPaymentRequest.orderId();
        String amount = confirmPaymentRequest.amount();
        String tossPaymentKey = confirmPaymentRequest.paymentKey();

        // 승인 요청에 사용할 JSON 객체를 만듭니다.
        JsonNode requestObj = objectMapper.createObjectNode()
                .put("orderId", tossOrderId)
                .put("amount", amount)
                .put("paymentKey", tossPaymentKey);

        // ObjectMapper를 사용하여 JSON 객체를 문자열로 변환
        String requestBody = objectMapper.writeValueAsString(requestObj);

        // Authorization 헤더 값 생성 (PaymentAuthInterceptor 사용)
        String authHeader = paymentAuthInterceptor.createPaymentAuthorizationHeader();

        // 결제 승인 API 요청 생성
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(paymentProperties.getBaseUrl() + "/v1/payments/confirm"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
