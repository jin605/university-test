package com.beyond.university.auth.controller;

import com.beyond.university.auth.model.dto.LoginRequestDto;
import com.beyond.university.auth.model.dto.LoginResponse;
import com.beyond.university.auth.model.service.AuthService;
import com.beyond.university.auth.model.service.JwtCookieService;
import com.beyond.university.common.model.dto.BaseResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/*
    인증 관련 API
    1. 로그인
      - POST /api/v1/auth/login

    2. 로그아웃
      - POST /api/v1/auth/logout

    3. 토큰 재발급
      - POST /api/v1/auth/refresh
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth APIs", description = "인증 관련 API 목록")
public class AuthController {
    private final AuthService authService;
    private final JwtCookieService jwtCookieService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "아이디와 패스워드를 JSON 문자열로 받아서 로그인한다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "BAD_REQUEST",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL_SERVER_ERROR",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    public ResponseEntity<BaseResponseDto<LoginResponse>> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto) {

        LoginResponse loginResponse = authService.login(
                loginRequestDto.getUsername(),
                loginRequestDto.getPassword()
        );
        String refreshToken = authService.createRefreshToken(loginResponse.getUsername());
        ResponseCookie cookie = jwtCookieService.createRefreshTokenCookie(refreshToken, Duration.ofDays(1));
        HttpHeaders headers = jwtCookieService.createRefreshTokenCookieHeaders(cookie);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new BaseResponseDto<>(HttpStatus.OK, loginResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "엑세스 토큰(Access Token)을 전달받아 로그아웃한다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "NO_CONTENT",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "UNAUTHORIZED",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL_SERVER_ERROR",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true) @RequestHeader("Authorization") String bearerToken) {

        authService.logout(bearerToken);

        ResponseCookie responseCookie = jwtCookieService.deleteRefreshTokenCookie();
        HttpHeaders headers = jwtCookieService.createRefreshTokenCookieHeaders(responseCookie);

        return ResponseEntity
                .noContent()
                .headers(headers)
                .build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰(Refresh Token)으로 엑세스 토큰(Access Token) 재발급")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "REFRESH_TOKEN_INVALID",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL_SERVER_ERROR",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    public ResponseEntity<BaseResponseDto<LoginResponse>> refreshToken(
           @Parameter(hidden = true) @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken) {

        LoginResponse loginResponse = authService.refreshAccessToken(refreshToken);

        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, loginResponse));
    }
}
