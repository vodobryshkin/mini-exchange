package io.github.vodobryshkin.miniexchange.service.impl;

import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import io.github.vodobryshkin.miniexchange.dto.orderbook.response.MarketDepthResponse;
import io.github.vodobryshkin.miniexchange.dto.orderbook.response.OrderBookLevelResponse;
import io.github.vodobryshkin.miniexchange.dto.orderbook.response.OrderBookResponse;
import io.github.vodobryshkin.miniexchange.exception.tradinginstrument.TradingInstrumentNotFoundException;
import io.github.vodobryshkin.miniexchange.repository.spec.IOrderBookRepository;
import io.github.vodobryshkin.miniexchange.repository.spec.ITradingInstrumentRepository;
import io.github.vodobryshkin.miniexchange.service.spec.IOrderBookService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация сервиса для получения биржевого стакана и расчёта его показателей.
 *
 * @author vodobryshkin
 * @since 19.11.2026 6:46
 */
@Service
@RequiredArgsConstructor
public class OrderBookService implements IOrderBookService {
    private final IOrderBookRepository orderBookRepository;
    private final ITradingInstrumentRepository tradingInstrumentRepository;

    /**
     * Метод для получения биржевого стакана по торговому инструменту.
     *
     * @param secCode код торгового инструмента.
     * @param depth максимальное количество ценовых уровней с каждой стороны.
     * @return DTO с биржевым стаканом и его показателями.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public OrderBookResponse getOrderBook(String secCode, int depth) {
        SecCode code = new SecCode(secCode);

        if (!tradingInstrumentRepository.existsTradingInstrumentBySecCode(code)) {
            throw new TradingInstrumentNotFoundException(secCode);
        }

        List<OrderBookLevelResponse> bids =
                orderBookRepository.findLevels(code, OrderDirection.BUY, depth);

        List<OrderBookLevelResponse> asks =
                orderBookRepository.findLevels(code, OrderDirection.SELL, depth);

        BigDecimal bestBid = bids.isEmpty() ? null : bids.getFirst().getPrice();

        BigDecimal bestAsk = asks.isEmpty() ? null : asks.getFirst().getPrice();

        BigDecimal spread = bestBid == null || bestAsk == null ? null : bestAsk.subtract(bestBid);

        return new OrderBookResponse(
                secCode,
                depth,
                bids,
                asks,
                bestBid,
                bestAsk,
                spread,
                new MarketDepthResponse(
                        sumQuantity(bids),
                        sumQuantity(asks)
                )
        );
    }

    private BigInteger sumQuantity(List<OrderBookLevelResponse> levels) {
        return levels.stream()
                .map(OrderBookLevelResponse::getQuantity)
                .reduce(BigInteger.ZERO, BigInteger::add);
    }
}