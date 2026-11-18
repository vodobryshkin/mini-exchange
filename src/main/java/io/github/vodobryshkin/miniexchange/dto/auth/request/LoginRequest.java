package io.github.vodobryshkin.miniexchange.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * DTO для запроса на вход пользователя.
 *
 * @author vodobryshkin
 * @since 18.11.2026 17:55
 */
@Data
public class LoginRequest {
    @NotBlank(message = "USERNAME_CANNOT_BE_BLANK")
    @Size(max = 32, message = "USERNAME_MUST_NOT_EXCEED_32_CHARACTERS")
    private String username;

    @NotBlank(message = "PASSWORD_CANNOT_BE_BLANK")
    @Size(max = 128, message = "PASSWORD_MUST_NOT_EXCEED_128_CHARACTERS")
    @ToString.Exclude
    private String password;
}