package io.github.vodobryshkin.miniexchange.exception.user;

/**
 * Исключение, которое выбрасывается, когда не удалось найти пользователя с указанным идентификатором.
 *
 * @author vodobryshkin
 * @since 18.11.2026 17:31
 */
public class UserNotFoundException extends RuntimeException {
    /**
     * Конструктор исключения.
     *
     * @param userId идентификатор пользователя.
     */
    public UserNotFoundException(long userId) {
        super("USER_NOT_FOUND: " + userId);
    }
}