package com.beyond.university.common.exception.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionMessage {
    DEPARTMENT_NOT_FOUND("학과 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS("아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_INVALID("리프레시 토큰이 유효하지 않습니다.", HttpStatus.BAD_REQUEST);

    private final String message;

    private final HttpStatus httpStatus;
}
