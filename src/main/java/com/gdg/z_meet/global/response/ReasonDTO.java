package com.gdg.z_meet.global.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ReasonDTO {

    private final HttpStatus status;
    private final String code;
    private final String message;
}
