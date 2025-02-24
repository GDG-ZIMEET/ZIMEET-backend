package com.gdg.z_meet.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class KaKaoPayReadyDTO {

    @NoArgsConstructor
    @Getter
    public static class Request {
        @NotNull
        private Long teamId;
        @NotNull
        private String productType;    // 상품 종류 (TWO_TO_TWO, THREE_TO_THREE , TICKET)
        @NotNull
        private Long totalPrice;     // 총 결제금액
    }

    @Getter
    @Builder
    public static class Parameter {
        private Long buyerId;           // userId -> 구매자 ID
        private Long teamId;
        private String productType;     // 상품 종류
        private Long totalPrice;        // 총 결제금액
        private Long vat;               // VAT (과세 10%)
    }

    @Getter
    @Builder
    public static class KakaoApiResponse {      // 카카오 서버 응답 데이터 -> 프론트
        private String tid;                     // 카카오페이 측 결제 고유 번호
        private String next_redirect_pc_url;    // 카카오페이가 생성한 결제 경로, 사용자는 해당 경로로 이동하여 결제 진행
    }

    @Getter
    @Builder
    public static class Response {
        private String nextRedirectPcUrl;
        private String orderId;
    }
}
