package com.gdg.z_meet.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Response<T> {

    private String code;
    private T data;
    private String message;

    public static Response<Void> ok() {
        Response<Void> response = new Response<>();

        response.code = Code.OK.getCode();
        response.message = Code.OK.getMessage();

        return response;
    }

    public static <T> Response<T> ok(T data) {
        Response<T> response = new Response<>();

        response.code = Code.OK.getCode();
        response.data = data;

        return response;
    }

    public static <T> Response<T>  fail(Code code) {
        Response<T> response = new Response<>();
        response.code = code.getCode();
        response.message = code.getMessage();
        response.data = null;
        return response;
    }

}
