package com.gdg.z_meet.domain.order;

import com.gdg.z_meet.domain.order.exception.PaymentErrorDecoder;
import com.gdg.z_meet.domain.order.infrastructure.PaymentAuthInterceptor;
import com.gdg.z_meet.domain.order.infrastructure.PaymentLoggingInterceptor;
import com.gdg.z_meet.domain.order.infrastructure.PaymentProperties;
import feign.Request;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

public class PaymentFeignConfig {

    private final PaymentProperties paymentProperties;

    public PaymentFeignConfig(PaymentProperties paymentProperties) {
        this.paymentProperties = paymentProperties;     // paymentProperties 클래스 내부에서 사용하기 위함
    }

    @Bean
    public Request.Options requestOptions() {
        // 요청 시간 지연 타임아웃 설정 => 자동 취소
        return new Request.Options(2, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true);
    }

    @Bean
    PaymentAuthInterceptor paymentAuthInterceptor() {
        // 요청에 Authorization 인증 헤더 추가
        return new PaymentAuthInterceptor(paymentProperties);
    }

    @Bean
    PaymentLoggingInterceptor paymentLoggingInterceptor() {
        return new PaymentLoggingInterceptor();
    }

    @Bean
    public PaymentErrorDecoder paymentErrorDecoder() {
        // Feign Client 호출 시 발생한 에러를 처리
        return new PaymentErrorDecoder();
    }
}