package com.gdg.z_meet.domain.order.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class PaymentErrorDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(PaymentErrorDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        // 기본 상태 코드와 응답 정보
        int status = response.status();
        String message = extractResponseBody(response);

        logger.error("Error occurred during Feign Client request: methodKey={}, status={}, message={}",
                methodKey, status, message);

        // 상태 코드별 예외 처리
        switch (status) {
            case 400:
                return new PaymentException(400, "Bad Request: " + message);
            case 401:
                return new PaymentException(401, "Unauthorized: " + message);
            case 403:
                return new PaymentException(403, "Forbidden: " + message);
            case 404:
                return new PaymentException(404, "Not Found: " + message);
            case 500:
                return new PaymentException(500, "Internal Server Error: " + message);
            default:
                // 기본 처리 (Feign의 기본 예외 처리 사용)
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }

    // 응답 바디 추출 메서드
    private String extractResponseBody(Response response) {
        try {
            if (response.body() != null) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8));
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            logger.error("Error while reading response body", e);
        }
        return "No response body";
    }

}
