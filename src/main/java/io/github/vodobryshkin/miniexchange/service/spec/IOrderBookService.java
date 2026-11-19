package io.github.vodobryshkin.miniexchange.service.spec;

import io.github.vodobryshkin.miniexchange.dto.orderbook.response.OrderBookResponse;

/**
 * Сервис для получения биржевого стакана и его показателей.
 *
 * @author vodobryshkin
 * @since 19.11.2026 06:45
 */
public interface IOrderBookService {
    /**
     * Метод для получения биржевого стакана по торговому инструменту.
     *
     * @param secCode код торгового инструмента.
     * @param depth максимальное количество ценовых уровней с каждой стороны.
     * @return DTO с биржевым стаканом и его показателями.
     */
    OrderBookResponse getOrderBook(String secCode, int depth);
}