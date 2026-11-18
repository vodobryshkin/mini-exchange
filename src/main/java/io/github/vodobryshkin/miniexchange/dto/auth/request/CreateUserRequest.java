package io.github.vodobryshkin.miniexchange.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для запроса на регистрацию пользователя.
 *
 * @author vodobryshkin
 * @since 18.11.2026 15:51
 */
@Data
public class CreateUserRequest {
    @NotNull
    @Size(min = 6, max = 32, message = "USERNAME_MUST_BE_BETWEEN_6_AND_32_CHARACTERS")
    @Pattern(regexp = "\\p{ASCII}*", message = "USERNAME_CAN_ONLY_CONTAIN_ASCII_CHARACTERS")
    private String username;

    @NotBlank(message = "PASSWORD_HASH_CANNOT_BE_BLANK")
    @Size(max = 255, message = "PASSWORD_HASH_MUST_NOT_EXCEED_255_CHARACTERS")
    private String passwordHash;
}
