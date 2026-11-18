package io.github.vodobryshkin.miniexchange.repository.spec;

import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;

/**
 * Интерфейс репозитория торговых инструментов, определяющий методы для работы с данными торговых инструментов.
 *
 * @author vodobryshkin
 * @since 18.11.2026 22:01
 */
public interface ITradingInstrumentRepository {
    /**
     * Метод для проверки, заведён ли в системе торговый инструмент с указанным кодом.
     *
     * @param secCode код торгового инструмента.
     * @return true, если торговый инструмент заведён в системе, false в противном случае.
     */
    boolean existsTradingInstrumentBySecCode(SecCode secCode);
}
