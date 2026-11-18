package io.github.vodobryshkin.miniexchange.service.spec;

import io.github.vodobryshkin.miniexchange.dto.auth.response.AuthResponse;
import io.github.vodobryshkin.miniexchange.dto.auth.request.CreateUserRequest;

/**
 * Интерфейс сервиса для выполнения операций, связанных с аутентификацией и авторизацией пользователей.
 *
 * @author vodobryshkin
 * @since 18.11.2026 15:37
 */
public interface IUserService {
    /**
     * Метод для регистрации нового пользователя.
     *
     * @param createUserRequest запрос на создание нового пользователя, содержащий имя пользователя и пароль.
     * @return DTO со статусом OK, если создание пользователя прошло успешно.
     */
    AuthResponse createUser(CreateUserRequest createUserRequest);
}
