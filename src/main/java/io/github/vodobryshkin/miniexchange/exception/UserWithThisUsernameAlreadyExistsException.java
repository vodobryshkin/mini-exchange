package io.github.vodobryshkin.miniexchange.exception;

/**
 * Исключение, выбрасываемое при попытке создать пользователя с уже существующим именем пользователя.
 *
 * @author vodobryshkin
 * @since 18.11.2026 16:17
 */
public class UserWithThisUsernameAlreadyExistsException extends RuntimeException {
    public UserWithThisUsernameAlreadyExistsException() {
        super("USER_ALREADY_EXISTS");
    }
}
