package com.beyond.university.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class LoginRequestDto {
    @NotBlank
    @Schema(title = "아이디", example = "admin")
    private final String username;

    @NotBlank
    @Schema(title = "비밀번호", example = "1234")
    private final String password;
}
