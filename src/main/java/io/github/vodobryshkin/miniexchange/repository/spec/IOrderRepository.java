package io.github.vodobryshkin.miniexchange.repository.spec;

import io.github.vodobryshkin.miniexchange.domain.order.Order;
import io.github.vodobryshkin.miniexchange.dto.order.request.OrderRegistryRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория заявок, определяющий методы для работы с данными заявок на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 21:36
 */
public interface IOrderRepository {
    /**
     * Метод для сохранения заявки в репозитории.
     *
     * @param order заявку, которую необходимо сохранить.
     */
    void save(@NotNull @Valid Order order);

    /**
     * Метод для того, чтобы пометить заявку в репозитории как отмененную.
     *
     * @param orderId id заявки, которую необходимо отменить.
     * @param userId id пользователя, который хочет отменить заявку.
     * @return true, если заявка была успешно отменена, false в противном случае.
     */
     boolean cancel(long orderId, long userId);

    /**
     * Метод для получения заявки по её идентификатору.
     *
     * @param id идентификатор заявки.
     * @return заявку с указанным идентификатором.
     */
    Optional<Order> findOrderById(long id);

    /**
     * Метод для подсчета заявок с учетом фильтров и прав доступа.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации.
     * @return общее количество подходящих заявок.
     */
    long countOrders(long userId, OrderRegistryRequest request);

    /**
     * Метод для получения страницы заявок по доступным пользователю счетам.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации и пагинации.
     * @return заявки в порядке убывания времени создания и идентификатора.
     */
    List<Order> findOrders(long userId, OrderRegistryRequest request);
}
