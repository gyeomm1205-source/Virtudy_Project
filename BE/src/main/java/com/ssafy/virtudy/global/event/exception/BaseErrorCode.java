package com.ssafy.virtudy.global.event.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BaseErrorCode {
    /**
     * 100 : 진행 정보
     */

    /**
     * 200 : 요청 성공
     */
    SUCCESS(HttpStatus.OK, "SUCCESS", "요청에 성공했습니다."),
    CREATED(HttpStatus.CREATED, "CREATED", "요청에 성공했으며 리소스가 정상적으로 생성되었습니다."),
    ACCEPTED(HttpStatus.ACCEPTED, "ACCEPTED", "요청에 성공했으나 처리가 완료되지 않았습니다."),
    DELETED(HttpStatus.NO_CONTENT, "DELETED", "요청에 성공했으며 더 이상 응답할 내용이 존재하지 않습니다."),

    /**
     * 300 : 리다이렉션
     */
    SEE_OTHER(HttpStatus.SEE_OTHER, "REDIRECT_001", "다른 주소로 요청해주세요."),
    RETRY_REQUEST(HttpStatus.FOUND, "REDIRECT_002", "재발급된 AccessToken / RefreshToken 으로 재시도 해주세요."),

    /**
     * 400 : 요청 실패
     */
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "REQUEST_ERROR_001", "잘못된 요청입니다."),
    INVALID_INPUT_DTO(HttpStatus.BAD_REQUEST, "REQUEST_ERROR_002", "잘못된 DTO 형식입니다."),
    INVALID_JWT(HttpStatus.UNAUTHORIZED, "REQUEST_ERROR_003", "로그인 후 이용해주세요."),
    INVALID_FILE(HttpStatus.BAD_REQUEST, "REQUEST_ERROR_004", "잘못된 File 형식입니다."),
    INVALID_AUTHORIZATION(HttpStatus.FORBIDDEN, "REQUEST_ERROR_005", "비정상적인 접근입니다."),
    INVALID_ENUM(HttpStatus.BAD_REQUEST, "REQUEST_ERROR_006", "변경할 수 없는 ENUM type 입니다."),
    TOO_MANY_REQUEST_ERROR(HttpStatus.TOO_MANY_REQUESTS, "REQUEST_ERROR_007", "요청이 너무 잦습니다. 잠시 후 다시 시도해주세요."),

    // Auth
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "AUTH_001", "이메일 형식이 올바르지 않습니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "AUTH_002", "비밀번호 형식이 올바르지 않습니다."),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "AUTH_003", "해당 닉네임은 중복입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_004", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005", "유효하지 않은 토큰입니다."),

    // Member
    MEMBER_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "MEMBER_001", "존재하지 않는 사용자입니다."),
    MEMBER_STATUS_NOT_VALID_ERROR(HttpStatus.NOT_FOUND, "MEMBER_002", "이미 삭제된 회원입니다"),
    JOB_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "MEMBER_003", "존재하지 않는 직업입니다."),
    DUPLICATED_MEMBER(HttpStatus.CONFLICT, "MEMBER_004", "이미 존재하는 사용자입니다."),

    // Report
    REPORT_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "REPORT_001", "존재하지 않는 리포트입니다."),
    SESSION_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "SESSION_001", "존재하지 않는 세션입니다."),

    // Reminder
    REMINDER_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "REMINDER_001", "존재하지 않는 알림입니다."),

    // Room
    ROOM_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND, "ROOM_001","종료되었거나 존재하지 않는 방입니다."),
    ROOM_FULL_ERROR(HttpStatus.BAD_REQUEST, "ROOM_002","방이 가득 찼습니다"),
    ROOM_NOT_AVAILABLE_ERROR(HttpStatus.BAD_REQUEST, "ROOM_003","입장 가능한 방이 없습니다."),
    ROOM_NOT_PARTICIPATE_ERROR(HttpStatus.BAD_REQUEST, "ROOM_004","현재 참여중인 방이 아닙니다."),
    ROOM_MAX_REACHED_ERROR(HttpStatus.BAD_REQUEST, "ROOM_005","방은 최대 3개까지 생성할 수 있습니다."),
    ROOM_PRIVATE_EMPTY_PASSWORD_ERROR(HttpStatus.BAD_REQUEST, "ROOM_006","비공개 방은 비밀번호가 필수입니다."),
    ROOM_PUBLIC_FILLED_PASSWORD_ERROR(HttpStatus.BAD_REQUEST, "ROOM_007","공개 방은 비밀번호가 존재하지 않아야 합니다."),
    ROOM_NOT_OWNER_ERROR(HttpStatus.BAD_REQUEST, "ROOM_008","방장만 접근할 수 있습니다."),

    /**
     * 500 : 응답 실패
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RESPONSE_ERROR_001", "서버와의 연결에 실패했습니다."),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "RESPONSE_ERROR_002", "다른 서버로부터 잘못된 응답이 수신되었습니다."),
    INSUFFICIENT_STORAGE(HttpStatus.INSUFFICIENT_STORAGE, "RESPONSE_ERROR_003", "서버의 용량이 부족해 요청에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
