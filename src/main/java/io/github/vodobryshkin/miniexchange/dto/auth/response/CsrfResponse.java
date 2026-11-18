package io.github.vodobryshkin.miniexchange.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO с CSRF-токеном и параметрами его передачи.
 *
 * @author vodobryshkin
 * @since 18.11.2026 18:00
 */
@Data
@AllArgsConstructor
public class CsrfResponse {
    private final String headerName;
    private final String parameterName;
    private final String token;
}