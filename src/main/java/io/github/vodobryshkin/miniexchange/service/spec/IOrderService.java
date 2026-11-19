package io.github.vodobryshkin.miniexchange.service.spec;

import io.github.vodobryshkin.miniexchange.dto.common.response.PageResponse;
import io.github.vodobryshkin.miniexchange.dto.order.request.CreateOrderRequest;
import io.github.vodobryshkin.miniexchange.dto.order.request.OrderRegistryRequest;
import io.github.vodobryshkin.miniexchange.dto.order.request.UpdateOrderRequest;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderOperationResponse;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderResponse;

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
     * @param request тело запроса на создание новой заявки.
     * @return DTO с информацией о созданной заявке.
     */
    OrderOperationResponse createOrder(long userId, CreateOrderRequest request);

    /**
     * Метод для отмены неисполненного остатка заявки.
     *
     * @param userId идентификатор пользователя, который хочет отменить заявку.
     * @param orderId идентификатор заявки.
     */
    void cancelOrder(long userId, long orderId);

    /**
     * Метод для обновления неисполненного остатка заявки.
     *
     * @param userId идентификатор пользователя, который хочет обновить заявку.
     * @param orderId идентификатор заявки.
     * @param request тело запроса на обновление заявки.
     * @return DTO с информацией об обновлённой заявке.
     */
    OrderResponse updateOrder(long userId, long orderId, UpdateOrderRequest request);

    /**
     * Метод для получения реестра заявок пользователя.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации и пагинации.
     * @return страница реестра заявок.
     */
    PageResponse<OrderResponse> getOrders(long userId, OrderRegistryRequest request);

    /**
     * Метод для выполнения одного шага исполнения очереди.
     *
     * @return true, если обработана заявка или заключена сделка.
     */
    boolean matchNext();

    /**
     * Метод для завершения действия одной просроченной заявки.
     *
     * @return true, если заявка переведена в EXPIRED.
     */
    boolean expireNext();
}