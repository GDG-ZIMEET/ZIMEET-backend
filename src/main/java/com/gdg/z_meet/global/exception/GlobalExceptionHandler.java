package com.gdg.z_meet.global.exception;

import com.gdg.z_meet.global.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    public Response<Void> businessExceptionHandler(BusinessException businessException) {

        log.error("BusinessException: {}", businessException.getCode());
        log.error("error: ", businessException);

        return Response.fail(businessException.getCode());
    }

    // 추가
}