package io.github.vodobryshkin.miniexchange.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для ответа на запрос авторизации пользователя.
 *
 * @author vodobryshkin
 * @since 18.11.2026 15:43
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String message;

    public static final AuthResponse OK = new AuthResponse("OK");
    public static final AuthResponse USER_ALREADY_EXISTS = new AuthResponse("USER_ALREADY_EXISTS");
}
