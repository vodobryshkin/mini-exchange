package io.github.vodobryshkin.miniexchange.security;

import io.github.vodobryshkin.miniexchange.domain.User;
import io.github.vodobryshkin.miniexchange.repository.spec.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Сервис загрузки пользователей для аутентификации.
 *
 * @author vodobryshkin
 * @since 18.11.2026 18:17
 */
@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {
    private final IUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("INVALID_CREDENTIALS"));

        return new ExchangeUserDetails(user.getId(), user.getUsername(), user.getPasswordHash());
    }
}
