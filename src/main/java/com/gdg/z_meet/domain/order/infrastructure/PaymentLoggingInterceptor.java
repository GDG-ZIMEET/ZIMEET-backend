package com.gdg.z_meet.domain.order.infrastructure;

import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;


@Configuration
public class PaymentLoggingInterceptor implements RequestInterceptor {

    // 결제 요청 로그 남기기
    private static final Logger logger = LoggerFactory.getLogger(PaymentLoggingInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        logger.info("Payment Request: {} {}", template.method(), template.url());
        logger.info("Payment Request Body: {}", new String(template.body()));
    }
}
