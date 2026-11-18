package io.github.vodobryshkin.miniexchange.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO с данными текущего пользователя.
 *
 * @author vodobryshkin
 * @since 18.11.2026 18:11
 */
@Data
@AllArgsConstructor
public class AuthenticatedUserResponse {
    private final long id;
    private final String username;
}