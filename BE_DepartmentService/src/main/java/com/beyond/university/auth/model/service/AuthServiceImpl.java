package com.beyond.university.auth.model.service;

import com.beyond.university.auth.jwt.JwtTokenProvider;
import com.beyond.university.auth.jwt.JwtUtil;
import com.beyond.university.auth.model.dto.LoginResponse;
import com.beyond.university.auth.model.mapper.AuthMapper;
import com.beyond.university.auth.model.vo.User;
import com.beyond.university.common.exception.UniversityException;
import com.beyond.university.common.exception.message.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;

    @Override

    public LoginResponse login(String username, String password) {
        // 사용자의 아이디와 비밀번호로 인증 처리를 진행한다.
        // 1. username으로 사용자를 조회
        User user = authMapper.selectUserByUsername(username);

        // 2. PasswordEncoder를 사용해 데이터베이스에 저장된 비밀번호와 입력받은 비밀번호가 일치하는지 확인
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new UniversityException(ExceptionMessage.INVALID_CREDENTIALS);
        }

        // 3. LoginResponse 객체를 생성해서 반환
        return createLoginResponse(user);
    }

    @Override
    public void logout(String bearerToken) {
        String accessToken = jwtTokenProvider.resolveToken(bearerToken);

        jwtTokenProvider.addBlacklist(accessToken);
        jwtTokenProvider.deleteRefreshToken(accessToken);
    }

    @Override
    public String createRefreshToken(String username) {

        return jwtTokenProvider.createRefreshToken(username);
    }

    @Override
    public LoginResponse refreshAccessToken(String refreshToken) {
        // 1. 리프레시 토큰 검증
        if (refreshToken.isBlank() || !jwtUtil.validateToken(refreshToken)) {
            throw new UniversityException(ExceptionMessage.REFRESH_TOKEN_INVALID);
        }

        // 2. 레디스(Redis)의 리프레시 토큰 비교
        if (!jwtTokenProvider.isValidRefreshToken(refreshToken)) {
            throw new UniversityException(ExceptionMessage.REFRESH_TOKEN_INVALID);
        }

        // 3. 사용자 정보를 조회 후 새로운 LoginResponse 객체를 생성
        User user = authMapper.selectUserByUsername(jwtUtil.getUsername(refreshToken));

        return createLoginResponse(user);
    }

    private LoginResponse createLoginResponse(User user) {
        // 사용자 권한 추출
        List<String> authorities =
                user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        // 엑세스 토큰(Access Token) 생성
        String accessToken =
                jwtTokenProvider.createAccessToken(user.getUsername(), authorities);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .type("Bearer")
                .username(user.getUsername())
                .authorities(authorities)
                .issuedAt(jwtUtil.getIssuedAt(accessToken))
                .expiresAt(jwtUtil.getExpiresAt(accessToken))
                .build();
    }
}
