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
    USER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "PROF-001", "Profile not found."),

    SESSION_EXPIRED(HttpStatus.INTERNAL_SERVER_ERROR, "PAY-000", "Session expired."),
    SESSION_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAY-001", "Session storage failed."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "PAY-002", "Invalid payment amount."),

    // Booth Error
    CLUB_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "BOOTH4001", "동아리가 이미 존재합니다."),
    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOTH4002", "동아리를 찾을 수 없습니다."),

    //Meeting Error
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING4001", "팀을 찾을 수 없습니다."),
    TEAM_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEETING4002", "팀원을 찾을 수 없습니다."),
    TEAM_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "MEETING4003", "팀원 수가 일치하지 않습니다."),
    INVALID_MY_TEAM_ACCESS(HttpStatus.BAD_REQUEST, "MEETING4004", "본인 팀은 조회할 수 없습니다."),
    INVALID_OTHER_TEAM_ACCESS(HttpStatus.BAD_REQUEST, "MEETING4005", "다른 팀은 조회할 수 없습니다."),
    NAME_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "MEETING4006", "이미 존재하는 팀명입니다."),
    TEAM_GENDER_MISMATCH(HttpStatus.BAD_REQUEST, "MEETING4007", "팀의 성별과 일치하지 않습니다."),
    TEAM_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "MEETING4008", "팀이 이미 존재합니다."),
    DELETE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "MEETING4009", "삭제 기회가 부족합니다."),
    TEAM_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MEETING4010", "팀 삭제가 실패하였습니다."),
    SEARCH_FILTER_NULL(HttpStatus.BAD_REQUEST, "MEETING4011", "검색 조건이 없습니다."),
    SEARCH_FILTER_EXCEEDED(HttpStatus.BAD_REQUEST, "MEETING4012", "검색 조건은 한 가지만 가능합니다."),

    //Hi Error
    HI_COUNT_ZERO(HttpStatus.BAD_REQUEST, "Hi4001","하이의 갯수가 0개 일 경우 하이를 보낼 수 없습니다."),
    SAME_GENDER(HttpStatus.BAD_REQUEST, "Hi4002", "같은 성별의 팀에게는 하이를 보낼 수 없습니다."),
    HI_DUPLICATION(HttpStatus.BAD_REQUEST, "Hi4003", "이미 하이를 보낸 팀입니다."),
    HI_NOT_FOUND(HttpStatus.NOT_FOUND,"Hi4004","하이를 찾을 수 없습니다."),

    // Chat Error
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND,"CHAT4001","채팅방을 찾을 수 없습니다."),
    JOINCHAT_ALREADY_EXIST(HttpStatus.BAD_REQUEST,"CHAT4002","이미 채팅방에 추가된 사용자입니다."),
    JOINCHAT_NOT_FOUND(HttpStatus.NOT_FOUND,"CHAT4003","해당 채팅방의 사용자를 찾을 수 없습니다."),


    INVALID_PRODUCT_TYPE(HttpStatus.BAD_REQUEST, "PRODUCT_4000", "잘못된 상품 유형입니다."),
    KAKAO_API_RESPONSE_ERROR(HttpStatus.BAD_GATEWAY, "KAKAO_5001", "카카오 API 응답 오류가 발생했습니다."),
    INVALID_KAKAO_API_RESPONSE(HttpStatus.BAD_GATEWAY, "KAKAO_5002", "잘못된 카카오 API 응답입니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_4004", "결제 정보를 찾을 수 없습니다."),
    KAKAO_API_INVALID_BUYER(HttpStatus.BAD_REQUEST, "KAKAO_4001", "결제자 정보가 일치하지 않습니다."),
    INVALID_TOTAL_PRICE(HttpStatus.BAD_REQUEST, "PRICE_4000", "잘못된 결제 금액입니다.");

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