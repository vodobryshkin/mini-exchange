package io.github.vodobryshkin.miniexchange.service.impl;

import io.github.vodobryshkin.miniexchange.domain.User;
import io.github.vodobryshkin.miniexchange.dto.auth.response.AuthResponse;
import io.github.vodobryshkin.miniexchange.dto.auth.request.CreateUserRequest;
import io.github.vodobryshkin.miniexchange.repository.spec.IUserRepository;
import io.github.vodobryshkin.miniexchange.service.spec.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Сервис для выполнения операций, связанных с авторизацией пользователей.
 *
 * @author vodobryshkin
 * @since 18.11.2026 15:49
 */
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Метод для регистрации нового пользователя.
     *
     * @param createUserRequest запрос на создание нового пользователя, содержащий имя пользователя и пароль.
     * @return DTO со статусом OK, если создание пользователя прошло успешно.
     */
    @Override
    public AuthResponse createUser(CreateUserRequest createUserRequest) {
        String username = createUserRequest.getUsername();
        String passwordHash = passwordEncoder.encode(createUserRequest.getPassword());

        userRepository.save(new User(username, passwordHash));

        return AuthResponse.OK;
    }
}
