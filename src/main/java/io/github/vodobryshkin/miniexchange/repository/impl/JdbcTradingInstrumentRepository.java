package io.github.vodobryshkin.miniexchange.repository.impl;

import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import io.github.vodobryshkin.miniexchange.repository.spec.ITradingInstrumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Реализация репозитория торговых инструментов с использованием JDBC.
 *
 * @author vodobryshkin
 * @since 18.11.2026 22:04
 */
@Repository
@RequiredArgsConstructor
public class JdbcTradingInstrumentRepository implements ITradingInstrumentRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Метод для проверки, заведён ли в системе торговый инструмент с указанным кодом.
     *
     * @param secCode код торгового инструмента.
     * @return true, если торговый инструмент заведён в системе, false в противном случае.
     */
    @Override
    public boolean existsTradingInstrumentBySecCode(SecCode secCode) {
        String sql = """
            SELECT EXISTS (
                SELECT 1 FROM trading_instruments
                WHERE sec_code = ?
            )
            """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, secCode.getValue()));
    }
}
