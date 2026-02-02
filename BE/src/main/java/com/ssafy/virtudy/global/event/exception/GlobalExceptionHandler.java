package com.ssafy.virtudy.global.event.exception;

import com.ssafy.virtudy.global.event.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j // 로그를 남기기 위해 사용
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. 우리가 직접 정의한 비즈니스 예외 처리 (BaseException)
     * 예: throw new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR);
     */
    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        log.warn("Business Exception: {}", e.getMessage()); // 경고 로그 남김
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    /**
     * 2. @Valid 실패 시 발생하는 예외 처리 (DTO 검증 실패)
     * BaseErrorCode.INVALID_INPUT_DTO 를 사용하여 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Validation Error: {}", e.getBindingResult().getFieldError().getDefaultMessage());
        // 필요하다면 e.getBindingResult()를 파싱해서 구체적인 필드 에러를 내려줄 수도 있음
        return ErrorResponse.toResponseEntity(BaseErrorCode.INVALID_INPUT_DTO);
    }

    /**
     * 3. 위에서 처리하지 못한 모든 예상치 못한 서버 에러 처리 (NullPointerException 등)
     * 보안을 위해 구체적인 에러 내용은 숨기고 INTERNAL_SERVER_ERROR 로 통일
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Error Occurred: ", e); // 에러 스택 트레이스 전체 로깅 (디버깅용)
        return ErrorResponse.toResponseEntity(BaseErrorCode.INTERNAL_SERVER_ERROR);
    }
}