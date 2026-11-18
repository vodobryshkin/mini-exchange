package io.github.vodobryshkin.miniexchange.repository.spec;

import io.github.vodobryshkin.miniexchange.domain.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Интерфейс репозитория пользователей, определяющий методы для работы с данными пользователей.
 *
 * @author vodobryshkin
 * @since 18.11.2026 15:25
 */
public interface IUserRepository {
    /**
     * Метод для сохранения пользователя в репозитории.
     *
     * @param user пользователь, которого необходимо сохранить.
     */
    void save(@NotNull @Valid User user);

    /**
     * Метод для поиска пользователя по его имени.
     *
     * @param username имя пользователя, по которому необходимо выполнить поиск.
     * @return Optional, содержащий найденного пользователя, или пустой Optional.
     */
    Optional<User> findUserByUsername(String username);

    /**
     * Метод для проверки, связан ли пользователь с указанным торговым счетом.
     *
     * @param id id пользователя.
     * @param tradingAccountId id торгового счета.
     * @return true, если пользователь связан с указанным торговым счетом, false в противном случае.
     */
    boolean linkedToThisTradingAccount(long id, long tradingAccountId);
}
