package io.github.vodobryshkin.miniexchange.repository.spec;

import io.github.vodobryshkin.miniexchange.domain.order.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

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
}
