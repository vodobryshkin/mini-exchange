package io.github.vodobryshkin.miniexchange.service.impl;

import io.github.vodobryshkin.miniexchange.domain.order.Order;
import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import io.github.vodobryshkin.miniexchange.dto.order.request.CreateOrderRequest;
import io.github.vodobryshkin.miniexchange.dto.order.request.UpdateOrderRequest;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderOperationResponse;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderResponse;
import io.github.vodobryshkin.miniexchange.exception.order.OrderNotCancelledException;
import io.github.vodobryshkin.miniexchange.exception.order.OrderNotFoundException;
import io.github.vodobryshkin.miniexchange.exception.tradingaccount.TradingAccountNotFoundException;
import io.github.vodobryshkin.miniexchange.exception.tradinginstrument.TradingInstrumentNotFoundException;
import io.github.vodobryshkin.miniexchange.mapper.OrderMapper;
import io.github.vodobryshkin.miniexchange.repository.spec.IOrderRepository;
import io.github.vodobryshkin.miniexchange.repository.spec.ITradingInstrumentRepository;
import io.github.vodobryshkin.miniexchange.repository.spec.IUserRepository;
import io.github.vodobryshkin.miniexchange.service.spec.IOrderService;
import io.github.vodobryshkin.miniexchange.dto.common.request.RegistryRequest;
import io.github.vodobryshkin.miniexchange.dto.common.response.PageResponse;
import io.github.vodobryshkin.miniexchange.dto.order.request.OrderRegistryRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Метод для отмены неисполненной заявки.
     *
     * @param userId идентификатор пользователя, который хочет отменить заявку.
     * @param orderId id запроса на отмену заявки.
     */
    @Override
    public void cancelOrder(long userId, long orderId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(String.valueOf(orderId)));

        if (!userRepository.linkedToThisTradingAccount(userId, order.getTradingAccountId())) {
            throw new TradingAccountNotFoundException(String.valueOf(order.getTradingAccountId()));
        }

        boolean cancelled = orderRepository.cancel(orderId, userId);

        if (!cancelled) {
            throw new OrderNotCancelledException("Failed to cancel order with ID: " + orderId);
        }
    }

    /**
     * Метод для обновления неисполненной заявки.
     *
     * @param userId  идентификатор пользователя, который хочет обновить заявку.
     * @param request тело запроса на обновление заявки.
     * @return DTO c информацией об обновленной заявке.
     */
    @Override
    public OrderResponse updateOrder(long userId, long orderId, UpdateOrderRequest request) {

        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(String.valueOf(orderId)));

        if (!userRepository.linkedToThisTradingAccount(userId, order.getTradingAccountId())) {
            throw new TradingAccountNotFoundException(String.valueOf(order.getTradingAccountId()));
        }

        order.setTimeInForce(request.getTimeInForce());
        order.setPrice(request.getPrice());
        order.setQuantity(request.getQuantity());

        orderRepository.save(order);

        order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(String.valueOf(orderId)));

        return orderMapper.toOrderResponse(order);
    }

    /**
     * Метод для получения реестра заявок пользователя.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации и пагинации.
     * @return страница реестра заявок.
     */
    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PageResponse<OrderResponse> getOrders(
            long userId,
            OrderRegistryRequest request) {

        RegistryRequest registry = request.getRegistry();

        List<OrderResponse> orders = orderRepository.findOrders(userId, request)
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();

        long totalElements = orderRepository.countOrders(userId, request);

        return new PageResponse<>(
                orders,
                registry.getPage(),
                registry.getSize(),
                totalElements
        );
    }
}
