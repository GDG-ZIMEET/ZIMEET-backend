package com.gdg.z_meet.domain.order.infrastructure;

import com.gdg.z_meet.domain.order.PaymentProperties;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;



// Toss Payments(외부 API) 호출 시, 요청 헤더에 사용자 인증을 위해 시크릿 키를 인코딩한 값을 넣어줘야
@Component
public class PaymentAuthInterceptor implements RequestInterceptor {

    // http 헤더에 사용자 Secret Key 를 Base64로 인코딩한 값을 넣어줘야.
    private static final String AUTH_HEADER_PREFIX = "Basic ";

    private final PaymentProperties paymentProperties;        // Secret Key 가져오는 곳

    public PaymentAuthInterceptor(final PaymentProperties paymentProperties) {
        this.paymentProperties = paymentProperties;
    }

    @Override
    public void apply(final RequestTemplate template) {
        final String authHeader = createPaymentAuthorizationHeader();
        template.header("Authorization", authHeader);     // 요청(Request)이 생성되기 전에 호출되며, HTTP 헤더에 Authorization 키를 추가
    }

    // Base64로 인코딩 & PREFIX 적용
    public String createPaymentAuthorizationHeader() {
        final byte[] encodedBytes = Base64.getEncoder().encode((paymentProperties.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8));

        return AUTH_HEADER_PREFIX + new String(encodedBytes);
    }

}
