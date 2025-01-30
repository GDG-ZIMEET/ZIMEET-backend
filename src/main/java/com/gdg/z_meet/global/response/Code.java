package com.gdg.z_meet.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum Code implements BaseCode {

    OK(HttpStatus.OK, "COMMON200", "성공입니다."),

    // Common Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러입니다. 관리자에게 문의하세요."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "찾을 수 없는 요청입니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEM-001", "Member not found."),
    MEMBER_EMAIL_UNAVAILABLE(HttpStatus.BAD_REQUEST, "MEM-002", "Email cannot used."),
    MEMBER_NICKNAME_UNAVAILABLE(HttpStatus.BAD_REQUEST, "MEM-003", "Nickname cannot used."),
    MEMBER_PASSWORD_UNAVAILABLE(HttpStatus.BAD_REQUEST, "MEM-004", "Password cannot used."),
    MEMBER_ALREADY_ON_PROCESS(HttpStatus.BAD_REQUEST, "MEM-999", "Member is already on process."),

    SESSION_EXPIRED(HttpStatus.INTERNAL_SERVER_ERROR, "PAY-000", "Session expired."),
    SESSION_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAY-001", "Session storage failed."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "PAY-002", "Invalid payment amount."),

    CLUB_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "BOOTH4001", "동아리가 이미 존재합니다."),
    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOTH4002", "동아리를 찾을 수 없습니다."),

    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING4001", "팀을 찾을 수 없습니다."),
    TEAM_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING4002", "팀원을 찾을 수 없습니다."),
    TEAM_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "MEETING4003", "팀원 수가 일치하지 않습니다."),
    ;

//    NOT_ACADEMY_EMAIL("EEM-001", "Email is not a university email."),
//    AUTH_CODE_NOT_MATCH("ATH-001", "Auth code not match."),
//    ACCESS_TOKEN_NOT_FOUND("ATH-002", "Auth token not found."),
//    REFRESH_TOKEN_NOT_FOUND("ATH-003", "Refresh token not found."),
//    MEMBER_LOGIN_SESSION_EXPIRED("ATH-004", "Auth session expired."),


    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .status(status)
                .code(code)
                .message(message)
                .build()
                ;
    }
}