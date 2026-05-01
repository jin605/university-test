package com.beyond.university.auth.jwt;

import com.beyond.university.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/*
    JwtUtil
      - JWT(JSON Web Token)와 관련된 유틸리티 역할을 하는 클래스이다.
      - JWT(JSON Web Token) 토큰 생성, 클래임 파싱, 유효성 검사 등을 수행한다.
 */
@Slf4j
@Component
public class JwtUtil {
    private final String issuer;
    private final SecretKey secretKey;

    public JwtUtil(JwtProperties jwtProperties) {
        log.info("JWT Issuer : {}", jwtProperties.getIssuer());
        log.info("JWT Secret : {}", jwtProperties.getSecret());

        this.issuer = jwtProperties.getIssuer();
        this.secretKey = new SecretKeySpec(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    // JWT(JSON Web Token) 토큰을 생성하는 메소드
    public String createJwtToken(Map<String, Object> claims, long expiration) {

        return Jwts.builder()
                .header().add("typ", "JWT").and() // typ 헤더 추가
                .claims(claims) // 공개 클래임
                .id(Long.toHexString(System.nanoTime())) // jti(JWT ID) 클래임
                .issuer(this.issuer) // 발급 주체
                .issuedAt(new Date()) // 발급 시간
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 만료 시간
                .signWith(this.secretKey) // 서명을 생성
                .compact();
    }

    // 클래임에서 JTI(JWT ID)를 추출하는 메소드
    public String getJti(String token) {

        return getClaims(token).getId();
    }

    // 클래임에서 username을 추출하는 메소드
    public String getUsername(String token) {

        return getClaims(token).get("username", String.class);
    }

    // 클래임에서 token_type을 추출하는 메소드
    public String getTokenType(String token) {

        return getClaims(token).get("token_type", String.class);
    }

    // 클래임에서 발급 시간(IssuedAt)을 추출하는 메소드
    public long getIssuedAt(String token) {

        return getClaims(token).getIssuedAt().getTime();
    }

    // 클래임에서 만료 시간(ExpiresAt)을 추출하는 메소드
    public long getExpiresAt(String token) {

        return getClaims(token).getExpiration().getTime();
    }

    // 토큰이 유효한지 확인하는 메소드 (토큰이 유효하면 true, 만료되었으면 false 반환)
    public boolean validateToken(String token) {

        // JWT(JSON Web Token)의 만료 시간을 현재 시간과 비교하여 토큰이 만료되었는지 확인한다.
        return getClaims(token).getExpiration().after(new Date());
    }

    // JWT(JSON Web Token)에서 클래임을 추출하는 메소드
    private Claims getClaims(String token) {
        // 토큰이 만료되면 parseSignedClaims() 메소드에서
        // ExpiredJwtException 예외가 발생하여 코드가 실행되지 않기 때문에
        // ExpiredJwtException 예외가 발생해도 클래임을 반환하도록 예외 처리를 한다.
        try {
            return Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
