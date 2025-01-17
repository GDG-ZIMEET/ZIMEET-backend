package com.gdg.z_meet.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Code {

    OK("COM-000", "Ok."),

    MEMBER_NOT_FOUND("MEM-001", "Member not found."),
    MEMBER_EMAIL_UNAVAILABLE("MEM-002", "Email cannot used."),
    MEMBER_NICKNAME_UNAVAILABLE("MEM-003", "Nickname cannot used."),
    MEMBER_PASSWORD_UNAVAILABLE("MEM-004", "Password cannot used."),
    MEMBER_ALREADY_ON_PROCESS("MEM-999", "Member is already on process."),

    SESSION_EXPIRED("PAY-000", "Session expired."),
    SESSION_STORAGE_FAILED("PAY-001", "Session storage failed."),
    INVALID_PAYMENT_AMOUNT("PAY-002", "Invalid payment amount."),


//    NOT_ACADEMY_EMAIL("EEM-001", "Email is not a university email."),
//    AUTH_CODE_NOT_MATCH("ATH-001", "Auth code not match."),
//    ACCESS_TOKEN_NOT_FOUND("ATH-002", "Auth token not found."),
//    REFRESH_TOKEN_NOT_FOUND("ATH-003", "Refresh token not found."),
//    MEMBER_LOGIN_SESSION_EXPIRED("ATH-004", "Auth session expired."),


    SERVER_ERROR("SEV-999", "Check the server.");

    private final String code;
    private final String message;

}