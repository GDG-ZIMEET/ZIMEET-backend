package com.gdg.z_meet.global.exception;

import com.gdg.z_meet.global.response.Code;
import com.gdg.z_meet.global.response.ReasonDTO;
import com.gdg.z_meet.global.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response<Void>> businessExceptionHandler(BusinessException ex) {

        log.error("BusinessException: {}", ex.getCode());
        log.error("error: ", ex);
        ReasonDTO reason = ex.getReason();
        return ResponseEntity
                .status(reason.getStatus())
                .body(Response.fail(ex.getCode()));
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<Response<Void>> generalExceptionHandler(GeneralException ex) {

        log.error("GeneralException: {}", ex.getMessage());
        ReasonDTO reason = ex.getReason();
        return ResponseEntity
                .status(reason.getStatus())
                .body(Response.fail(Code.valueOf(reason.getCode())));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.fail(Code.BAD_REQUEST, errors));

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleAllException(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.fail(Code.INTERNAL_SERVER_ERROR));
    }
}

//
//@Override
//protected ResponseEntity<Object> handleExceptionInternal(
//        Exception ex,
//        Object body,
//        HttpHeaders headers,
//        HttpStatusCode status,
//        WebRequest request) {
//
//    if (body == null) {
//        body = Code.INTERNAL_SERVER_ERROR.getReason();
//    }
//
//    return new ResponseEntity<>(body, headers, status);
//}
//
//protected ResponseEntity<Object> handleExceptionInternal(Exception e, ReasonDTO reason,
//                                                       HttpHeaders headers, HttpServletRequest request) {
//
//    Response<Object> body = Response.fail(reason.getCode());
//
//    WebRequest webRequest = new ServletWebRequest(request);
//    return super.handleExceptionInternal(
//            e,
//            body,
//            headers,
//            reason.getHttpStatus(),
//            webRequest
//    );
//}