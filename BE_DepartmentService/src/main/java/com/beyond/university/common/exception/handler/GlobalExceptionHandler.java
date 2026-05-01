package com.beyond.university.common.exception.handler;

import com.beyond.university.common.exception.UniversityException;
import com.beyond.university.common.exception.dto.ApiErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
    스프링 예외 처리
    1. @ControllerAdvice
      - Controller 빈에서 발생하는 예외를 전역으로 처리할 수 있다.

    2. @RestControllerAdvice
      - RestController 빈에서 발생하는 예외를 전역으로 처리할 수 있다.

    3. @ExceptionHandler
      - 컨트롤러에서 발생하는 예외를 처리하는 메소드를 정의할 때 사용한다.
      - 메소드에서 처리할 예외를 어노테이션의 value 속성으로 지정한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UniversityException.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(UniversityException e) {
        ApiErrorResponseDto apiErrorResponseDto = new ApiErrorResponseDto(
                e.getHttpStatus().value(),
                e.getStatus(),
                e.getMessage()
        );

        log.error("UniversityException : {}", e.getMessage());

        return new ResponseEntity<>(apiErrorResponseDto, e.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(MethodArgumentNotValidException e) {
        StringBuilder errors = new StringBuilder();

        log.error("MethodArgumentNotValidException : {}", e.getMessage());

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors
                    .append(fieldError.getField())
                    .append("(")
                    .append(fieldError.getDefaultMessage())
                    .append("), ");
        }

        errors.replace(errors.lastIndexOf(","),  errors.length(), "");

        return new ResponseEntity<>(
                new ApiErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.name(),
                        errors.toString()
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(Exception e) {
        ApiErrorResponseDto apiErrorResponseDto = new ApiErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                e.getMessage()
        );

        log.error("Global Exception : {}", e.getMessage());


        return new ResponseEntity<>(apiErrorResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
