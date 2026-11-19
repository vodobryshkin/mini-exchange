package io.github.vodobryshkin.miniexchange.repository.spec;

import io.github.vodobryshkin.miniexchange.domain.order.Order;
import io.github.vodobryshkin.miniexchange.dto.order.TradeResources;
import io.github.vodobryshkin.miniexchange.dto.order.request.OrderRegistryRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
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
     * Метод для получения заявки по её идентификатору.
     *
     * @param id идентификатор заявки.
     * @return заявку с указанным идентификатором.
     */
    Optional<Order> findOrderById(long id);

    /**
     * Метод для подсчёта заявок с учётом фильтров и прав доступа.
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

    /**
     * Метод для блокировки торговых изменений до завершения транзакции.
     */
    void lockMarket();

    /**
     * Метод для получения следующего номера очереди.
     *
     * @return номер постановки заявки в очередь.
     */
    long nextSequence();

    /**
     * Метод для проверки валюты, шага цены и кратности количества.
     *
     * @param order проверяемая заявка.
     * @return true, если параметры соответствуют правилам инструмента.
     */
    boolean validTerms(Order order);

    /**
     * Метод для получения первой ожидающей обработки заявки.
     *
     * @return заблокированную заявку, если очередь не пуста.
     */
    Optional<Order> findPending();

    /**
     * Метод для получения наиболее приоритетной встречной заявки.
     *
     * @param incoming входящая заявка.
     * @param now момент проверки срока действия.
     * @return заблокированную встречную заявку, если она существует.
     */
    Optional<Order> findCounterparty(Order incoming, Instant now);

    /**
     * Метод для получения первой просроченной активной заявки.
     *
     * @param now момент проверки срока действия.
     * @return заблокированную заявку, если она существует.
     */
    Optional<Order> findExpired(Instant now);

    /**
     * Метод для блокировки счетов и получения средств сторон сделки.
     *
     * @param buy заявка покупки.
     * @param sell заявка продажи.
     * @return средства и сведения о владельцах сторон.
     */
    TradeResources lockResources(Order buy, Order sell);

    /**
     * Метод для сохранения сделки и изменения денег и позиций.
     *
     * @param buy заявка покупки.
     * @param sell заявка продажи.
     * @param price цена исполнения.
     * @param quantity количество исполнения.
     * @param now момент исполнения.
     */
    void settle(Order buy, Order sell, BigDecimal price,
                long quantity, Instant now);
}