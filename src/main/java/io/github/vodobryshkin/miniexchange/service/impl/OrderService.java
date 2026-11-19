package io.github.vodobryshkin.miniexchange.service.impl;

import io.github.vodobryshkin.miniexchange.domain.order.Order;
import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.order.OrderStatus;
import io.github.vodobryshkin.miniexchange.domain.order.OrderType;
import io.github.vodobryshkin.miniexchange.domain.order.TimeInForce;
import io.github.vodobryshkin.miniexchange.dto.common.request.RegistryRequest;
import io.github.vodobryshkin.miniexchange.dto.common.response.PageResponse;
import io.github.vodobryshkin.miniexchange.dto.order.request.CreateOrderRequest;
import io.github.vodobryshkin.miniexchange.dto.order.request.OrderRegistryRequest;
import io.github.vodobryshkin.miniexchange.dto.order.request.UpdateOrderRequest;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderOperationResponse;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderResponse;
import io.github.vodobryshkin.miniexchange.exception.order.OrderNotActiveException;
import io.github.vodobryshkin.miniexchange.exception.order.OrderNotFoundException;
import io.github.vodobryshkin.miniexchange.exception.order.OrderRejectedException;
import io.github.vodobryshkin.miniexchange.exception.tradingaccount.TradingAccountNotFoundException;
import io.github.vodobryshkin.miniexchange.exception.tradinginstrument.TradingInstrumentNotFoundException;
import io.github.vodobryshkin.miniexchange.mapper.OrderMapper;
import io.github.vodobryshkin.miniexchange.repository.spec.IOrderRepository;
import io.github.vodobryshkin.miniexchange.repository.spec.ITradingInstrumentRepository;
import io.github.vodobryshkin.miniexchange.repository.spec.IUserRepository;
import io.github.vodobryshkin.miniexchange.service.spec.IOrderService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
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
    private final ITradingInstrumentRepository instrumentRepository;
    private final OrderMapper orderMapper;
    private final Clock clock;
    private final LocalTime tradingDayEndTime;
    private final ZoneId tradingZone;

    /**
     * Метод для создания новой заявки на бирже.
     *
     * @param userId идентификатор пользователя, создающего заявку.
     * @param request тело запроса на создание новой заявки.
     * @return DTO с информацией о созданной заявке.
     */
    @Override
    @Transactional
    public OrderOperationResponse createOrder(
            long userId,
            CreateOrderRequest request) {

        orderRepository.lockMarket();

        Instant now = clock.instant();

        Order order = Order.builder()
                .secCode(request.getSecCode())
                .tradingAccountId(request.getTradingAccountId())
                .direction(request.getDirection())
                .type(request.getType())
                .timeInForce(request.getTimeInForce())
                .price(request.getPrice())
                .initialQuantity(request.getQuantity())
                .quantity(request.getQuantity())
                .status(OrderStatus.NEW)
                .creationTime(now)
                .expiresAt(expiresAt(now))
                .queueSequence(orderRepository.nextSequence())
                .matchingPending(true)
                .build();

        validateTerms(userId, order);
        orderRepository.save(order);

        return OrderOperationResponse.CREATED;
    }

    /**
     * Метод для отмены неисполненного остатка заявки.
     *
     * @param userId идентификатор пользователя, который хочет отменить заявку.
     * @param orderId идентификатор заявки.
     */
    @Override
    @Transactional
    public void cancelOrder(long userId, long orderId) {
        orderRepository.lockMarket();

        Order order = ownedOrder(userId, orderId);
        requireActive(order);

        finish(order, OrderStatus.CANCELLED);
    }

    /**
     * Метод для обновления неисполненного остатка заявки.
     *
     * @param userId идентификатор пользователя, который хочет обновить заявку.
     * @param orderId идентификатор заявки.
     * @param request тело запроса с новым общим количеством и ценой заявки.
     * @return DTO с информацией об обновлённой заявке.
     */
    @Override
    @Transactional
    public OrderResponse updateOrder(
            long userId,
            long orderId,
            UpdateOrderRequest request) {

        orderRepository.lockMarket();

        Order order = ownedOrder(userId, orderId);
        requireActive(order);

        if (request.getTimeInForce() != TimeInForce.DAY) {
            throw new OrderRejectedException("UNSUPPORTED_TIME_IN_FORCE");
        }

        if (request.getQuantity() <= order.getFilledQuantity()) {
            throw new OrderRejectedException(
                    "QUANTITY_MUST_EXCEED_FILLED_QUANTITY"
            );
        }

        boolean losesPriority = request.getPrice().compareTo(order.getPrice()) != 0
                || request.getQuantity() > order.getQuantity();

        order.setPrice(request.getPrice());
        order.setQuantity(request.getQuantity());

        if (losesPriority) {
            order.setQueueSequence(orderRepository.nextSequence());
        }

        order.setMatchingPending(true);

        validateTerms(userId, order);
        orderRepository.save(order);

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

        return new PageResponse<>(
                orders,
                registry.getPage(),
                registry.getSize(),
                orderRepository.countOrders(userId, request)
        );
    }

    /**
     * Метод для выполнения одного шага исполнения очереди.
     *
     * @return true, если обработана заявка или заключена сделка.
     */
    @Override
    @Transactional
    public boolean matchNext() {
        orderRepository.lockMarket();

        Optional<Order> pending = orderRepository.findPending();

        if (pending.isEmpty()) {
            return false;
        }

        Order incoming = pending.get();

        if (!incoming.getExpiresAt().isAfter(clock.instant())) {
            finish(incoming, OrderStatus.EXPIRED);
            return true;
        }

        Optional<Order> candidate = orderRepository.findCounterparty(
                incoming,
                clock.instant()
        );

        if (candidate.isEmpty()) {
            incoming.setMatchingPending(false);
            orderRepository.save(incoming);
            return true;
        }

        Order maker = candidate.get();

        Order buy = incoming.getDirection() == OrderDirection.BUY
                ? incoming
                : maker;

        Order sell = incoming.getDirection() == OrderDirection.SELL
                ? incoming
                : maker;

        long quantity = Math.min(
                buy.getRemainingQuantity(),
                sell.getRemainingQuantity()
        );

        var resources = orderRepository.lockResources(buy, sell);

        Instant now = clock.instant();

        BigDecimal cost = maker.getPrice()
                .multiply(BigDecimal.valueOf(quantity));

        if (!incoming.getExpiresAt().isAfter(now)) {
            finish(incoming, OrderStatus.EXPIRED);
        } else if (!maker.getExpiresAt().isAfter(now)) {
            finish(maker, OrderStatus.EXPIRED);
        } else if (resources.isSameOwner()) {
            finish(incoming, OrderStatus.CANCELLED);
        } else if (resources.getBuyerBalance().compareTo(cost) < 0) {
            finish(buy, OrderStatus.CANCELLED);
        } else if (resources.getSellerQuantity() < quantity) {
            finish(sell, OrderStatus.CANCELLED);
        } else {
            orderRepository.settle(
                    buy,
                    sell,
                    maker.getPrice(),
                    quantity,
                    now
            );

            applyFill(buy, quantity);
            applyFill(sell, quantity);
        }

        return true;
    }

    /**
     * Метод для завершения действия одной просроченной заявки.
     *
     * @return true, если заявка переведена в EXPIRED.
     */
    @Override
    @Transactional
    public boolean expireNext() {
        orderRepository.lockMarket();

        Optional<Order> expired = orderRepository.findExpired(clock.instant());

        if (expired.isEmpty()) {
            return false;
        }

        finish(expired.get(), OrderStatus.EXPIRED);
        return true;
    }

    /**
     * Метод для получения заявки, принадлежащей пользователю.
     *
     * @param userId идентификатор пользователя.
     * @param orderId идентификатор заявки.
     * @return заявку пользователя.
     */
    private Order ownedOrder(long userId, long orderId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        String.valueOf(orderId)
                ));

        if (!userRepository.linkedToThisTradingAccount(
                userId,
                order.getTradingAccountId())) {

            throw new TradingAccountNotFoundException(
                    String.valueOf(order.getTradingAccountId())
            );
        }

        return order;
    }

    /**
     * Метод для проверки возможности изменения активного остатка заявки.
     *
     * @param order проверяемая заявка.
     */
    private void requireActive(Order order) {
        if (order.getStatus() != OrderStatus.NEW
                || order.getRemainingQuantity() <= 0
                || !order.getExpiresAt().isAfter(clock.instant())) {

            throw new OrderNotActiveException(
                    String.valueOf(order.getId())
            );
        }
    }

    /**
     * Метод для проверки владельца счёта и параметров заявки.
     *
     * @param userId идентификатор пользователя.
     * @param order проверяемая заявка.
     */
    private void validateTerms(long userId, Order order) {
        if (!userRepository.linkedToThisTradingAccount(
                userId,
                order.getTradingAccountId())) {

            throw new TradingAccountNotFoundException(
                    String.valueOf(order.getTradingAccountId())
            );
        }

        if (!instrumentRepository.existsTradingInstrumentBySecCode(
                order.getSecCode())) {

            throw new TradingInstrumentNotFoundException(
                    order.getSecCode().getValue()
            );
        }

        if (order.getType() != OrderType.LIMIT
                || order.getTimeInForce() != TimeInForce.DAY) {

            throw new OrderRejectedException("ONLY_LIMIT_DAY_IS_SUPPORTED");
        }

        if (!orderRepository.validTerms(order)) {
            throw new OrderRejectedException(
                    "INVALID_CURRENCY_PRICE_OR_LOT_SIZE"
            );
        }
    }

    /**
     * Метод для получения ближайшей границы торгового дня после приёма заявки.
     *
     * @param acceptedAt момент приёма заявки.
     * @return момент истечения DAY-заявки.
     */
    private Instant expiresAt(Instant acceptedAt) {
        ZonedDateTime accepted = acceptedAt.atZone(tradingZone);

        ZonedDateTime boundary = accepted.toLocalDate()
                .atTime(tradingDayEndTime)
                .atZone(tradingZone);

        if (!boundary.toInstant().isAfter(acceptedAt)) {
            boundary = accepted.toLocalDate()
                    .plusDays(1)
                    .atTime(tradingDayEndTime)
                    .atZone(tradingZone);
        }

        return boundary.toInstant();
    }

    /**
     * Метод для завершения действия остатка заявки.
     *
     * @param order заявку, действие которой необходимо завершить.
     * @param status конечный статус заявки.
     */
    private void finish(Order order, OrderStatus status) {
        order.setStatus(status);
        order.setMatchingPending(false);

        orderRepository.save(order);
    }

    /**
     * Метод для отражения исполненного количества в заявке.
     *
     * @param order исполненную заявку.
     * @param quantity количество исполнения.
     */
    private void applyFill(Order order, long quantity) {
        order.setFilledQuantity(Math.addExact(
                order.getFilledQuantity(),
                quantity
        ));

        if (order.getRemainingQuantity() == 0) {
            order.setStatus(OrderStatus.FILLED);
            order.setMatchingPending(false);
        }

        orderRepository.save(order);
    }
}