package io.github.vodobryshkin.miniexchange.service.impl;

import io.github.vodobryshkin.miniexchange.domain.order.Order;
import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import io.github.vodobryshkin.miniexchange.dto.order.request.CreateOrderRequest;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderOperationResponse;
import io.github.vodobryshkin.miniexchange.exception.tradingaccount.TradingAccountNotFoundException;
import io.github.vodobryshkin.miniexchange.exception.tradinginstrument.TradingInstrumentNotFoundException;
import io.github.vodobryshkin.miniexchange.mapper.OrderMapper;
import io.github.vodobryshkin.miniexchange.repository.spec.IOrderRepository;
import io.github.vodobryshkin.miniexchange.repository.spec.ITradingInstrumentRepository;
import io.github.vodobryshkin.miniexchange.repository.spec.IUserRepository;
import io.github.vodobryshkin.miniexchange.service.spec.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Сервис для обработки жизненного цикла заявок на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 21:33
 */
@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final IOrderRepository orderRepository;
    private final IUserRepository userRepository;
    private final ITradingInstrumentRepository tradingInstrumentRepository;

    private final OrderMapper orderMapper;

    /**
     * Метод для создания новой заявки на бирже.
     *
     * @param userId идентификатор пользователя, создающего заявку.
     * @param request запрос на создание новой заявки.
     * @return DTO с информацией о созданной заявке.
     */
    @Override
    public OrderOperationResponse createOrder(long userId, CreateOrderRequest request) {
        long tradingAccountId = request.getTradingAccountId();
        SecCode secCode = request.getSecCode();

        if (!userRepository.linkedToThisTradingAccount(userId, tradingAccountId)) {
            throw new TradingAccountNotFoundException(String.valueOf(tradingAccountId));
        }

        if (!tradingInstrumentRepository.existsTradingInstrumentBySecCode(secCode)) {
            throw new TradingInstrumentNotFoundException(String.valueOf(secCode.getValue()));
        }

        Order order = orderMapper.toOrder(request);

        orderRepository.save(order);

        return OrderOperationResponse.CREATED;
    }
}
