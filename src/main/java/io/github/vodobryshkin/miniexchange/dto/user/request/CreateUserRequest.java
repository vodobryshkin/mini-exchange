package io.github.vodobryshkin.miniexchange.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * DTO для запроса на регистрацию пользователя.
 *
 * @author vodobryshkin
 * @since 18.11.2026 15:51
 */
@Data
public class CreateUserRequest {
    /**
     * Имя пользователя.
     *
     * @mock buyer_demo
     */
    @NotNull(message = "USERNAME_CANNOT_BE_NULL")
    @Size(min = 6, max = 32, message = "USERNAME_MUST_BE_BETWEEN_6_AND_32_CHARACTERS")
    @Pattern(regexp = "[A-Za-z0-9_]+",
            message = "USERNAME_CAN_ONLY_CONTAIN_LATIN_LETTERS_DIGITS_AND_UNDERSCORE")
    private String username;

    /**
     * Пароль пользователя.
     *
     * @mock Demo_Password_2026
     */
    @NotBlank(message = "PASSWORD_CANNOT_BE_BLANK")
    @Size(min = 15, max = 128,
            message = "PASSWORD_MUST_BE_BETWEEN_15_AND_128_CHARACTERS")
    @ToString.Exclude
    private String password;
}
