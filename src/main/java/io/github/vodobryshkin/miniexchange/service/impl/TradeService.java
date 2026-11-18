package io.github.vodobryshkin.miniexchange.service.impl;

import io.github.vodobryshkin.miniexchange.dto.common.request.RegistryRequest;
import io.github.vodobryshkin.miniexchange.dto.common.response.PageResponse;
import io.github.vodobryshkin.miniexchange.dto.trade.response.TradeResponse;
import io.github.vodobryshkin.miniexchange.mapper.TradeMapper;
import io.github.vodobryshkin.miniexchange.repository.spec.ITradeRepository;
import io.github.vodobryshkin.miniexchange.service.spec.ITradeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для получения реестра заключенных сделок.
 *
 * @author vodobryshkin
 * @since 19.11.2026 02:19
 */
@Service
@RequiredArgsConstructor
public class TradeService implements ITradeService {
    private final ITradeRepository tradeRepository;
    private final TradeMapper tradeMapper;

    /**
     * Метод для получения реестра сделок пользователя.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации и пагинации.
     * @return страница реестра сделок.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PageResponse<TradeResponse> getTrades(long userId, RegistryRequest request) {
        List<TradeResponse> trades = tradeRepository.findTrades(userId, request)
                .stream()
                .map(tradeMapper::toTradeResponse)
                .toList();

        long totalElements = tradeRepository.countTrades(userId, request);

        return new PageResponse<>(
                trades,
                request.getPage(),
                request.getSize(),
                totalElements
        );
    }
}