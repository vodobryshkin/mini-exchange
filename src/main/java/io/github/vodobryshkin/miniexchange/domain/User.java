package io.github.vodobryshkin.miniexchange.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Класс, представляющий пользователя приложения.
 *
 * @author vodobryshkin
 * @since 18.11.2026 12:43
 */
@AllArgsConstructor
@Getter
public class User {
    private Long id;

    @NotNull(message = "USERNAME_CANNOT_BE_NULL")
    @Size(min = 6, max = 32, message = "USERNAME_MUST_BE_BETWEEN_6_AND_32_CHARACTERS")
    @Pattern(
            regexp = "[A-Za-z0-9_]+",
            message = "USERNAME_CAN_ONLY_CONTAIN_LATIN_LETTERS_DIGITS_AND_UNDERSCORE"
    )
    private String username;

    @NotBlank(message = "PASSWORD_HASH_CANNOT_BE_BLANK")
    @Size(max = 255, message = "PASSWORD_HASH_MUST_NOT_EXCEED_255_CHARACTERS")
    private String passwordHash;

    /**
     * Конструктор пользователя без идентификатора.
     *
     * @param username имя пользователя.
     * @param passwordHash хеш пароля.
     */
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
}
