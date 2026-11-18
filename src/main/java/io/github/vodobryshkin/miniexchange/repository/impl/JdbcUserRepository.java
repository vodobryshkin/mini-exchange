package io.github.vodobryshkin.miniexchange.repository.impl;

import io.github.vodobryshkin.miniexchange.domain.User;
import io.github.vodobryshkin.miniexchange.exception.UserWithThisUsernameAlreadyExistsException;
import io.github.vodobryshkin.miniexchange.repository.spec.IUserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Реализация репозитория пользователей, использующая чистый JDBC для взаимодействия с базой данных.
 *
 * @author vodobryshkin
 * @since 18.11.2026 15:29
 */
@Repository
@RequiredArgsConstructor
public class JdbcUserRepository implements IUserRepository {
    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) ->
            new User(
                    rs.getLong("user_id"),
                    rs.getString("username"),
                    rs.getString("password_hash")
            );

    private final JdbcTemplate jdbcTemplate;

    /**
     * Метод для сохранения пользователя в репозитории.
     *
     * @param user Пользователь, которого необходимо сохранить.
     */
    @Override
    public void save(User user) {
        if (user.getId() != null) {
            String sql = """
                    UPDATE users
                    SET username = ?, password_hash = ?
                    WHERE user_id = ?
                    """;

            jdbcTemplate.update(sql, user.getUsername(), user.getPasswordHash(), user.getId());

            return;
        }

        String sql = """
                INSERT INTO users (username, password_hash)
                VALUES (?, ?)
                ON CONFLICT (username) DO NOTHING
                """;

        String username = user.getUsername();
        String passwordHash = user.getPasswordHash();

        int result = jdbcTemplate.update(sql, username, passwordHash);

        if (result == 0) {
            throw new UserWithThisUsernameAlreadyExistsException();
        }
    }

    /**
     * Метод для поиска пользователя по его имени.
     *
     * @param username имя пользователя, по которому необходимо выполнить поиск.
     * @return Optional, содержащий найденного пользователя, или пустой Optional.
     */
    @Override
    public Optional<User> findUserByUsername(String username) {
        String sql = """
                SELECT user_id, username, password_hash FROM users
                WHERE username = ?
                """;

        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, username);

        return users.stream().findFirst();
    }
}