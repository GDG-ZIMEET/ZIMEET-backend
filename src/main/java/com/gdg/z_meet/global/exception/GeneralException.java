package com.gdg.z_meet.global.exception;

import com.gdg.z_meet.global.response.BaseCode;
import com.gdg.z_meet.global.response.ReasonDTO;

public class GeneralException extends RuntimeException {

    private BaseCode code;

    public ReasonDTO getReason() {
        return this.code.getReason();
    }
}
