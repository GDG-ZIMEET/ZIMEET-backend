package com.gdg.z_meet.domain.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.z_meet.domain.order.controller.TossPaymentController;
import com.gdg.z_meet.domain.order.dto.request.ConfirmPaymentReq;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * TOSS API 와 연관된 외부 로직 처리 Service
 * TOSS 서버 입장에서는 Spring WAS 또한 Client
 */
@Service
public class TossPaymentClient {

    // JSON 처리
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Toss Payments 서버에 결제 승인 요청
     */
    public HttpResponse<String> requestConfirm(ConfirmPaymentReq confirmPaymentReq) throws IOException, InterruptedException {

        String tossOrderId = confirmPaymentReq.getOrderId();     // Toss Payments 서버 에서 사용할 ID
        String amount = confirmPaymentReq.getAmount();
        String tossPaymentKey = confirmPaymentReq.getPaymentKey();

        // 승인 요청에 사용할 JSON 객체 생성
        JsonNode requestObj = objectMapper.createObjectNode()
                .put("orderId", tossOrderId)
                .put("amount", amount)
                .put("paymentKey", tossPaymentKey);

        // JSON 객체를 문자열로 변환 => Toss 서버로 보낼 승인 요청 requestBody
        String requestBody = objectMapper.writeValueAsString(requestObj);


        // 결제 승인 API 호출
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/confirm"))
                .header("Authorization", getAuthorizations())
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Toss Payments 서버로 HTTP 요청 전송
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Toss Payments 서버에 결제 취소 요청
     */
    public HttpResponse<String> requestPaymentCancel(String paymentKey, String cancelReason) throws IOException, InterruptedException {
        System.out.println(paymentKey);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel"))
                .header("Authorization", getAuthorizations())
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\"cancelReason\":\"" + cancelReason + "\"}"))
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }


    /**
     * Toss Payments 서버에 요청을 위한 인증 정보 생성
     */
    private static String getAuthorizations() {
        Base64.Encoder encoder = Base64.getEncoder();

        // Toss Payments 시크릿 키에 : 를 추가한 후 Base64 인코딩
        byte[] encodedBytes = encoder.encode((TossPaymentController.widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedBytes);
    }

}
