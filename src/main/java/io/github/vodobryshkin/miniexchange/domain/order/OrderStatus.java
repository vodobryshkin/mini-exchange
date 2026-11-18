package io.github.vodobryshkin.miniexchange.domain.order;

/**
 * Все возможные статусы заявки на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 14:00
 */
public enum OrderStatus {
    NEW,
    FILLED,
    CANCELED,
    EXPIRED
}
