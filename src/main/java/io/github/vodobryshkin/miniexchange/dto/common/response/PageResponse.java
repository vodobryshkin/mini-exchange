package io.github.vodobryshkin.miniexchange.dto.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для представления страницы реестра.
 *
 * @param <T> тип записи реестра.
 * @author vodobryshkin
 * @since 19.11.2026 00:47
 */
@Data
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> items;

    private int page;

    private int size;

    @JsonProperty("total_elements")
    private long totalElements;
}