package io.github.vodobryshkin.miniexchange.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vodobryshkin.miniexchange.config.SecurityConfig;
import io.github.vodobryshkin.miniexchange.dto.user.request.CreateUserRequest;
import io.github.vodobryshkin.miniexchange.dto.auth.response.AuthResponse;
import io.github.vodobryshkin.miniexchange.security.ExchangeUserDetails;
import io.github.vodobryshkin.miniexchange.service.spec.IUserService;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты для AuthController.
 *
 * @author vodobryshkin
 * @since 18.11.2026 19:31
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private static Stream<Arguments> validUsernamesAndPasswords() {
        return Stream.of(
                Arguments.of("vladimir", "0123456789qwertyu"),
                Arguments.of("vova12", "0123456789qwertyu"),
                Arguments.of("00000000000000000000000000000000", "0000000000000000")
        );
    }

    @ParameterizedTest
    @MethodSource("validUsernamesAndPasswords")
    void registerShouldReturnCreatedForValidUsernameAndPassword(String username, String password) throws Exception {
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(AuthResponse.OK);

        String content = "{\"username\": \"" + username + "\" , \"password\": \"" + password + "\"}";

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("OK"));

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    void registerShouldRejectShortUsername() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "abc",
                                "password": "0123456789qwertyu"
                            }
                            """))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(userService);
    }

    @Test
    void registerShouldRejectRequestWithoutCsrf() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "vladimir",
                                    "password": "0123456789qwertyu"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("CSRF_INVALID"));

        verifyNoInteractions(userService);
    }

    @Test
    void meShouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("AUTHENTICATION_REQUIRED"));
    }

    private void prepareUser() {
        String passwordHash = passwordEncoder.encode("0123456789qwertyu");
        when(userDetailsService.loadUserByUsername("vladimir"))
                .thenReturn(new ExchangeUserDetails(1L, "vladimir", passwordHash));
    }

    @Test
    void loginShouldAuthenticateAndPreserveSession() throws Exception {
        prepareUser();

        MvcResult csrfResult = mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode csrfBody = objectMapper.readTree(csrfResult.getResponse().getContentAsString());
        String headerName = csrfBody.get("headerName").asText();
        String token = csrfBody.get("token").asText();

        MockHttpSession session = (MockHttpSession) csrfResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        String previousSessionId = session.getId();

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .session(session)
                        .header(headerName, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "vladimir",
                                "password": "0123456789qwertyu"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andReturn();

        MockHttpSession authenticatedSession = (MockHttpSession) loginResult.getRequest().getSession(false);

        assertAll(
                () -> assertThat(authenticatedSession).isNotNull(),
                () -> assertThat(authenticatedSession.getId()).isNotEqualTo(previousSessionId)
        );

        mockMvc.perform(get("/auth/me").session(authenticatedSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("vladimir"));

        mockMvc.perform(post("/auth/logout")
                        .session(authenticatedSession)
                        .header(headerName, token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("CSRF_INVALID"));
    }

    @Test
    void loginShouldRejectWrongPassword() throws Exception {
        prepareUser();

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "vladimir",
                                "password": "0987654321mnbvcx"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginShouldRejectRequestWithoutCsrf() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "vladimir",
                                "password": "0123456789qwertyu"
                            }
                            """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("CSRF_INVALID"));

        verifyNoInteractions(userDetailsService);
    }
}

