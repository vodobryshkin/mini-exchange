package io.github.vodobryshkin.miniexchange.dto.common.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Общие параметры запроса на получение реестра.
 *
 * @author vodobryshkin
 * @since 19.11.2026 00:35
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryRequest {
    @Positive(message = "TRADING_ACCOUNT_ID_MUST_BE_POSITIVE")
    private Long tradingAccountId;

    @Pattern(
            regexp = "[A-Z0-9._/-]{1,32}",
            message = "SEC_CODE_INVALID"
    )
    private String secCode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime to;

    /**
     * Номер страницы.
     *
     * @mock 0
     */
    @Builder.Default
    @NotNull(message = "PAGE_CANNOT_BE_NULL")
    @Min(value = 0, message = "PAGE_MUST_BE_NON_NEGATIVE")
    private Integer page = 0;

    /**
     * Размер страницы.
     *
     * @mock 50
     */
    @Builder.Default
    @NotNull(message = "SIZE_CANNOT_BE_NULL")
    @Min(value = 1, message = "SIZE_MUST_BE_POSITIVE")
    @Max(value = 100, message = "SIZE_MUST_NOT_EXCEED_100")
    private Integer size = 50;

    /**
     * Метод для проверки границ периода с учетом часовых поясов.
     *
     * @return true, если границы периода допустимы.
     */
    @JsonIgnore
    @AssertTrue(message = "FROM_MUST_BE_BEFORE_TO")
    public boolean isPeriodValid() {
        return from == null
                || to == null
                || from.toInstant().isBefore(to.toInstant());
    }

    /**
     * Метод для получения смещения страницы без переполнения типа int.
     *
     * @return количество записей, которые необходимо пропустить.
     */
    @JsonIgnore
    public long getOffset() {
        return (long) page * size;
    }
}
