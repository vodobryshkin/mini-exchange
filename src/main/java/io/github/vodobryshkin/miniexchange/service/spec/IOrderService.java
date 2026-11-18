package io.github.vodobryshkin.miniexchange.service.spec;

import io.github.vodobryshkin.miniexchange.dto.order.request.CreateOrderRequest;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderOperationResponse;

/**
 * Сервис для работы с заявками на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 21:25
 */
public interface IOrderService {
    /**
     * Метод для создания новой заявки на бирже.
     *
     * @param userId идентификатор пользователя, создающего заявку.
     * @param request запрос на создание новой заявки.
     * @return DTO с информацией о созданной заявке.
     */
    OrderOperationResponse createOrder(long userId, CreateOrderRequest request);

    /**
     * Метод для отмены неисполненной заявки.
     *
     * @param userId идентификатор пользователя, который хочет отменить заявку.
     * @param orderId id на отмену заявки.
     */
    void cancelOrder(long userId, long orderId);
}
