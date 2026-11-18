package io.github.vodobryshkin.miniexchange.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Класс, представляющий пользователя приложения.
 *
 * @author vodobryshkin
 * @since 18.11.2026 12:43
 */
public class User {
    private Long id;

    @NotNull
    @Size(min = 6, max = 32, message = "USERNAME_MUST_BE_BETWEEN_6_AND_32_CHARACTERS")
    @Pattern(regexp = "\\p{ASCII}*", message = "USERNAME_CAN_ONLY_CONTAIN_ASCII_CHARACTERS")
    private String username;

    @NotBlank(message = "PASSWORD_HASH_CANNOT_BE_BLANK")
    @Size(max = 255, message = "PASSWORD_HASH_MUST_NOT_EXCEED_255_CHARACTERS")
    private String passwordHash;
}
