package com.beyond.university.auth.model.service;

import com.beyond.university.auth.model.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(String username, String password);

    void logout(String bearerToken);

    String createRefreshToken(String username);

    LoginResponse refreshAccessToken(String refreshToken);
}
