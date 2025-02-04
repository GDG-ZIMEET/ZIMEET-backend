package com.gdg.z_meet.domain.order.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdg.z_meet.domain.order.dto.request.CancelPaymentReq;
import com.gdg.z_meet.domain.order.dto.request.ConfirmPaymentReq;
import com.gdg.z_meet.domain.order.dto.response.ConfirmPaymentRes;
import com.gdg.z_meet.domain.order.dto.ConfirmSuccessPaymentInfo;
import com.gdg.z_meet.domain.order.entity.TossPaymentMethod;
import com.gdg.z_meet.domain.order.entity.TossPaymentStatus;
import com.gdg.z_meet.domain.order.service.TossPaymentClient;
import com.gdg.z_meet.domain.order.service.TossPaymentService;
import com.gdg.z_meet.global.response.Code;
import com.gdg.z_meet.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class TossPaymentController {

    // 토스가 제공하는 테스트용 시크릿 키
    public static final String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";

    // JSON 처리
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TossPaymentService tossPaymentService;
    private final TossPaymentClient tossPaymentClient;

    /**
     * Order 테이블의 ID로 결제정보를 조회
     */
    @GetMapping("/{id}")
    public Response<?> getPayment(@PathVariable("id") String backendOrderId) {
        ConfirmPaymentRes payment = tossPaymentService.getPayment(backendOrderId);

        return Response.of(Code.PAYMENT_SUCCESS, payment);
    }

    @PostMapping("/confirm")
    public Response<?> confirmPayment(@RequestBody ConfirmPaymentReq confirmPaymentReq) throws Exception {

        // Toss 서버에 결제 승인 요청
        HttpResponse<String> response = tossPaymentClient.requestConfirm(confirmPaymentReq);

        int code = response.statusCode();
        boolean isSuccess = code == 200;

        String responseBody = response.body();                                // 결제 승인 응답 바디 추출
        JsonNode responseObject = objectMapper.readTree(responseBody);        // 문자열 -> JSON 파싱


        String backendOrderId;
        String tossOrderId;
        String paymentKey = "";
        String paymentMethod;
        String paymentStatus;
        LocalDateTime requestedAt;
        LocalDateTime approvedAt;

        try{
            if(isSuccess){
                backendOrderId = confirmPaymentReq.getBackendOrderId();    // 결제 주문 ID
                tossOrderId = responseObject.get("orderId").asText();     // 토스 주문 ID
                paymentKey = responseObject.get("paymentKey").asText();
                paymentMethod = responseObject.get("method").asText();
                paymentStatus = responseObject.get("status").asText();
                requestedAt = OffsetDateTime.parse(responseObject.get("requestedAt").asText()).toLocalDateTime();
                approvedAt = OffsetDateTime.parse(responseObject.get("approvedAt").asText()).toLocalDateTime();
                long totalAmount = responseObject.get("totalAmount").asLong();

                ConfirmSuccessPaymentInfo confirmSuccessPaymentInfo = ConfirmSuccessPaymentInfo.create(
                        backendOrderId,
                        tossOrderId,
                        paymentKey,
                        TossPaymentMethod.valueOf(paymentMethod),
                        TossPaymentStatus.valueOf(paymentStatus),
                        totalAmount,
                        requestedAt,
                        approvedAt
                );

                // 결제 정보 저장
                ConfirmPaymentRes confirmPaymentRes = tossPaymentService.addPayment(confirmSuccessPaymentInfo);
                return Response.of(Code.PAYMENT_SUCCESS, confirmPaymentRes);
            }
        }catch(RuntimeException e){
            if(!paymentKey.isEmpty()){
                tossPaymentClient.requestPaymentCancel(paymentKey, "서버 DB 오류");
            }
            return Response.of(Code.PAYMENT_FAILED, "DB 오류 발생, 결제가 취소되었습니다.");
        }

        // 결제 승인 실패 시 응답
        return Response.of(Code.PAYMENT_FAILED,responseObject);
    }


    // 결제 취소
    @PostMapping("/cancel")
    public Response<?> cancelPayment(@RequestBody CancelPaymentReq cancelPaymentReq) throws IOException, InterruptedException {
        HttpResponse<String> response = tossPaymentClient.requestPaymentCancel(cancelPaymentReq.getPaymentKey(), cancelPaymentReq.getCancelReason());

        if (response.statusCode() == 200) {
            tossPaymentService.changePaymentStatus(cancelPaymentReq.getPaymentKey(), TossPaymentStatus.CANCELED);
            return Response.of(Code.PAYMENT_CANCELED, response.body());
        }

        return Response.of(Code.PAYMENT_FAILED, response.body());
    }


}