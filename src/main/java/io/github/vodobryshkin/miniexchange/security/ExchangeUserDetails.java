package io.github.vodobryshkin.miniexchange.security;

import java.io.Serial;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.userdetails.User;

/**
 * Пользователь Spring Security с идентификатором из базы данных.
 *
 * @author vodobryshkin
 * @since 18.11.2026 18:05
 */
@Getter
public class ExchangeUserDetails extends User {
    @Serial
    private static final long serialVersionUID = 1L;

    private final long id;

    /**
     * Конструктор пользователя Spring Security.
     *
     * @param id идентификатор пользователя.
     * @param username имя пользователя.
     * @param passwordHash хеш пароля.
     */
    public ExchangeUserDetails(long id, String username, String passwordHash) {
        super(username, passwordHash, List.of());
        this.id = id;
    }
}
