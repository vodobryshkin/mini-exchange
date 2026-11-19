package io.github.vodobryshkin.miniexchange.domain.tradinginstrument;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Класс, представляющий код торгового инструмента.
 *
 * @author vodobryshkin
 * @since 18.11.2026 14:10
 */
@Getter
@RequiredArgsConstructor
public class SecCode {
    /**
     * Код торгового инструмента.
     *
     * @mock SBER
     */
    @NotNull(message = "SEC_CODE_CANNOT_BE_NULL")
    @Size(min = 1, max = 32, message = "SEC_CODE_MUST_BE_BETWEEN_1_AND_32_CHARACTERS")
    @Pattern(regexp = "[A-Z0-9._/-]+", message = "SEC_CODE_CAN_ONLY_CONTAIN_UPPERCASE_LETTERS_NUMBERS_AND_SYMBOLS")
    private final String value;
}
