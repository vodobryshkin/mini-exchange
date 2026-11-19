package io.github.vodobryshkin.miniexchange.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vodobryshkin.miniexchange.dto.auth.response.AuthResponse;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Конфигурация безопасности приложения.
 *
 * @author vodobryshkin
 * @since 18.11.2026 17:40
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final ObjectMapper objectMapper;

    /**
     * Метод для создания кодировщика паролей Argon2.
     *
     * @return кодировщик паролей.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 19 * 1024, 2);
    }

    /**
     * Метод для создания менеджера аутентификации.
     *
     * @param userDetailsService сервис загрузки пользователей.
     * @param passwordEncoder кодировщик для проверки паролей.
     * @return менеджер аутентификации.
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /**
     * Метод для хранения контекста безопасности в сессии.
     *
     * @return репозиторий контекста безопасности.
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * Метод для хранения CSRF-токена в сессии.
     *
     * @return репозиторий CSRF-токенов.
     */
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    /**
     * Метод для настройки сессии после успешного входа.
     *
     * @param csrfTokenRepository репозиторий CSRF-токенов.
     * @return стратегия смены идентификатора сессии и сброса CSRF-токена.
     */
    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(CsrfTokenRepository csrfTokenRepository) {
        return new CompositeSessionAuthenticationStrategy(List.of(
                new ChangeSessionIdAuthenticationStrategy(),
                new CsrfAuthenticationStrategy(csrfTokenRepository)
        ));
    }

    /**
     * Метод для настройки cookie сессии.
     *
     * @param secureCookie признак передачи cookie только по HTTPS.
     * @return сериализатор cookie сессии.
     */
    @Bean
    public DefaultCookieSerializer cookieSerializer(@Value("${app.security.secure-cookie:false}")
                                                        boolean secureCookie) {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookiePath("/");
        serializer.setUseSecureCookie(secureCookie);
        return serializer;
    }

    /**
     * Метод для настройки правил безопасности HTTP-запросов.
     *
     * @param http конфигурация безопасности HTTP.
     * @param securityContextRepository репозиторий контекста безопасности.
     * @param csrfTokenRepository репозиторий CSRF-токенов.
     * @return цепочка фильтров безопасности.
     * @throws Exception если не удалось настроить цепочку фильтров.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SecurityContextRepository securityContextRepository,
                                                   CsrfTokenRepository csrfTokenRepository) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeResponse(response, HttpStatus.UNAUTHORIZED, AuthResponse.AUTHENTICATION_REQUIRED))
                        .accessDeniedHandler((request, response, exception) -> {
                            AuthResponse body = exception instanceof CsrfException ? AuthResponse.CSRF_INVALID : AuthResponse.FORBIDDEN;
                            writeResponse(response, HttpStatus.FORBIDDEN, body);
                        }))
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .deleteCookies("SESSION")
                        .logoutSuccessHandler((request, response, authentication) ->
                                writeResponse(response, HttpStatus.OK, AuthResponse.OK)));

        return http.build();
    }

    private void writeResponse(HttpServletResponse response, HttpStatus status, AuthResponse body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}