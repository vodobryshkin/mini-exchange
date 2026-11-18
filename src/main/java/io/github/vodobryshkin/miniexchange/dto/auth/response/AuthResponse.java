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
    private final String message;

    public static final AuthResponse OK = new AuthResponse("OK");
    public static final AuthResponse USER_ALREADY_EXISTS = new AuthResponse("USER_ALREADY_EXISTS");
    public static final AuthResponse INVALID_CREDENTIALS = new AuthResponse("INVALID_CREDENTIALS");
    public static final AuthResponse AUTHENTICATION_REQUIRED = new AuthResponse("AUTHENTICATION_REQUIRED");
    public static final AuthResponse FORBIDDEN = new AuthResponse("FORBIDDEN");
    public static final AuthResponse CSRF_INVALID = new AuthResponse("CSRF_INVALID");
}
