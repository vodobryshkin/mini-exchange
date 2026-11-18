package io.github.vodobryshkin.miniexchange.controller;

import io.github.vodobryshkin.miniexchange.dto.auth.request.CreateUserRequest;
import io.github.vodobryshkin.miniexchange.dto.auth.response.AuthResponse;
import io.github.vodobryshkin.miniexchange.service.spec.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер, на который будут приходить запросы на аутентификацию пользователей.
 *
 * @author vodobryshkin
 * @since 18.11.2026 16:44
 */
@RestController
@RequestMapping(
        value = "/auth",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class AuthController {
    private final IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody @Valid CreateUserRequest createUserRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(createUserRequest));
    }
}
