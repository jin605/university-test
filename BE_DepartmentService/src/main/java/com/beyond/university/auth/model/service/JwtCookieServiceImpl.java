package com.beyond.university.auth.model.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class JwtCookieServiceImpl implements JwtCookieService {
    /*
        ResponseCookie
          - 스프링 프레임워크에서 제공하는 클래스이다.
          - 서버에서 HTTP 응답에 담길 쿠키를 생성할 때 사용한다.
     */
    @Override
    public ResponseCookie createRefreshTokenCookie(String refreshToken, Duration duration) {

        return ResponseCookie
                .from("refresh_token", refreshToken)
                // 자바스크립트에서 쿠키에 접근 불가능
                .httpOnly(true)
                // 쿠키의 유효 경로
                .path("/")
                // 쿠키의 수명 설정
                .maxAge(duration)
                .build();
    }

    @Override
    public ResponseCookie deleteRefreshTokenCookie() {

        return createRefreshTokenCookie("", Duration.ofSeconds(0));
    }

    /*
        HttpHeaders
          - 스프링 프레임워크에서 제공하는 클래스이다.
          - HTTP 요청/응답의 헤더를 생성할 때 사용한다.
     */
    @Override
    public HttpHeaders createRefreshTokenCookieHeaders(ResponseCookie cookie) {
        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

        return headers;
    }
}
